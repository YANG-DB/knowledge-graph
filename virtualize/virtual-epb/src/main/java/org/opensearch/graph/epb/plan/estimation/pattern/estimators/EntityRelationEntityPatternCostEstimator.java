package org.opensearch.graph.epb.plan.estimation.pattern.estimators;



import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.epb.plan.estimation.CostEstimationConfig;
import org.opensearch.graph.epb.plan.estimation.pattern.EntityRelationEntityPattern;
import org.opensearch.graph.epb.plan.estimation.pattern.Pattern;
import org.opensearch.graph.epb.plan.estimation.IncrementalEstimationContext;
import org.opensearch.graph.epb.plan.statistics.StatisticsProvider;
import org.opensearch.graph.epb.plan.statistics.StatisticsProviderFactory;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.*;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.CountEstimatesCost;
import org.opensearch.graph.model.execution.plan.costs.DoubleCost;
import org.opensearch.graph.model.execution.plan.costs.DetailedCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationFilterOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.ontology.OntologyFinalizer;
import org.opensearch.graph.model.query.properties.*;
import org.opensearch.graph.model.resourceInfo.FuseError;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class EntityRelationEntityPatternCostEstimator implements PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> {
    //region Static
    /**
     * ********************************************************
     * Calculate estimates for a full pattern.
     * Algorithm description:
     * Pattern = E1 ------- Rel + filter(+ E2 pushdown props) ------> E2 + filter( without pushdown props)
     * <p>
     * N1 = Prior estimate for E1 count
     * <p>
     * calculations:
     * R1 (relation estimate based on E1 count and global selectivity) = N1 * GS
     * R2 (relation filter estimate) = statistical_estimate(Rel + filter + E1 pushdown)
     * R (rel estimate) = min(R1, R2)
     * <p>
     * Z (E2 node pushdown props only estimate) = statistical_estimate(E1 pushdown filter only)
     * alpha - GS ratio, > 1
     * N2-1 (relation based estimate for E2) = min(R*alpha/GS, Z)
     * N2-2 (E2 complete estimate) = statistical_estimate(E1 + filter (with pushdown))
     * <p>
     * N2 = min(N2-1, N2-2)
     * <p>
     * countsUpdateFactors = (R/R1)*(N2/N2-1)
     * N1' = countsUpdateFactors*N1 (back propagate count estimate)
     * ********************************************************
     * @param config
     * @param statisticsProvider
     * @param previousPlanCost
     * @param step
     * @return
     */
    public static PatternCostEstimator.Result<Plan, CountEstimatesCost> calculateFullStep(
            CostEstimationConfig config,
            StatisticsProvider statisticsProvider,
            PlanWithCost<Plan, PlanDetailedCost> previousPlanCost,
            EntityRelationEntityPattern step) {

        EntityOp start = step.getStart();
        EntityFilterOp startFilter = step.getStartFilter();
        EntityOp end = step.getEnd();
        EntityFilterOp endFilter = step.getEndFilter();
        RelationOp rel = step.getRel();
        RelationFilterOp relationFilter = step.getRelFilter();

        PlanDetailedCost previousCost = previousPlanCost.getCost();
        CountEstimatesCost entityOneCost = previousCost.getPlanStepCost(start).get().getCost();

        //edge estimate =>
        Direction direction = Direction.of(rel.getAsgEbase().geteBase().getDir());
        //(relation estimate based on E1 count and global selectivity) = N1 * GS
        long selectivity = statisticsProvider.getGlobalSelectivity(rel.getAsgEbase().geteBase(),
                relationFilter.getAsgEbase().geteBase(),
                start.getAsgEbase().geteBase(), direction);
        double N1 = entityOneCost.peek();
        double R1 = N1 * selectivity;
        //(relation filter estimate) = statistical_estimate(Rel + filter + E1 pushdown)
        double R2 = statisticsProvider.getEdgeFilterStatistics(relationFilter.getRel().geteBase(), relationFilter.getAsgEbase().geteBase(),start.getAsgEbase().geteBase() ).getTotal();
        //(rel estimate) = min(R1, R2)
        double R = Math.min(R1, R2);

        EPropGroup clone = endFilter.getAsgEbase().geteBase().clone();
        List<RelProp> pushdownProps = new LinkedList<>();
        List<RelProp> collect = relationFilter.getAsgEbase().geteBase().getProps().stream().filter(f -> (f instanceof RedundantRelProp) &&
                (!f.getpType().equals(OntologyFinalizer.ID_FIELD_PTYPE) &&
                        (!f.getpType().equals(OntologyFinalizer.TYPE_FIELD_PTYPE))))
                .collect(Collectors.toList());
        collect.forEach(p -> {
            pushdownProps.add(p);
            clone.getProps().add(EProp.of(p.geteNum(), p.getpType(), p.getCon()));
        });

        // Z (E2 node pushdown props only estimate) = statistical_estimate(E1 pushdown filter only)
        double Z = statisticsProvider.getRedundantNodeStatistics(end.getAsgEbase().geteBase(), RelPropGroup.of(pushdownProps)).getTotal();

        // N2-1 (relation based estimate for E2) = min(R*alpha/GS, Z)
        double N2_1 = Math.min(R * config.getAlpha(), Z);
        //N2_2 (E2 complete estimate) = statistical_estimate(E1 + filter (with pushdown))
        double N2_2 = statisticsProvider.getNodeFilterStatistics(end.getAsgEbase().geteBase(), clone).getTotal();

        //* N2 = min(N2-1, N2-2)
        double N2 = Math.min(N2_1, N2_2);

        //estimate back propagation weight
        double lambdaEdge = R / R2;
        double lambdaNode = N2 / N2_2;
        // countsUpdateFactors = (R/R1)*(N2/N2-1)
        double lambda = lambdaEdge * lambdaNode;

        //estimation if zero since the real estimation is residing on the adjacent filter (rel filter)
        DoubleCost relCost = new DetailedCost(R * config.getDelta(), lambdaNode, lambdaEdge, R, N2);

        CountEstimatesCost newEntityOneCost = new CountEstimatesCost(entityOneCost.getCost(), Math.ceil(N1));
        newEntityOneCost.push(Math.ceil(N1*lambda));
        PlanWithCost<Plan, CountEstimatesCost> entityOnePlanCost ;
        if(startFilter == null){
            entityOnePlanCost = new PlanWithCost<>(new Plan(start), newEntityOneCost);
        } else{
            entityOnePlanCost = new PlanWithCost<>(new Plan(start, startFilter), newEntityOneCost);
        }

        CountEstimatesCost newRelCost = new CountEstimatesCost(relCost.getCost(), Math.ceil(R));
        PlanWithCost<Plan, CountEstimatesCost> relPlanCost = new PlanWithCost<>(new Plan(rel, relationFilter), newRelCost);

        CountEstimatesCost entityTwoCost = new CountEstimatesCost(Math.ceil(N2), Math.ceil(N2));
        PlanWithCost<Plan, CountEstimatesCost> entityTwoOpCost = new PlanWithCost<>(new Plan(end, endFilter), entityTwoCost);

        return PatternCostEstimator.Result.of(new double[]{lambda}, entityOnePlanCost, relPlanCost, entityTwoOpCost);
    }
    //endregion

    //region Constructors
    public EntityRelationEntityPatternCostEstimator(
            CostEstimationConfig config,
            StatisticsProviderFactory statisticsProviderFactory,
            OntologyProvider ontologyProvider) {
        this.config = config;
        this.statisticsProviderFactory = statisticsProviderFactory;
        this.ontologyProvider = ontologyProvider;
    }
    //endregion

    //region StepPatternCostEstimator Implementation
    @Override
    public PatternCostEstimator.Result<Plan, CountEstimatesCost> estimate(Pattern pattern, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery> context) {
        if (!EntityRelationEntityPattern.class.isAssignableFrom(pattern.getClass())) {
            return PatternCostEstimator.EmptyResult.get();
        }

        StatisticsProvider statisticsProvider = this.statisticsProviderFactory.get(this.ontologyProvider.get(context.getQuery().getOnt())
                .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No target Ontology field found ", "No target Ontology found for " + context.getQuery().getOnt()))));
        return calculateFullStep(config, statisticsProvider, context.getPreviousCost().get(), (EntityRelationEntityPattern) pattern);
    }
    //endregion

    //region Fields
    private CostEstimationConfig config;
    private StatisticsProviderFactory statisticsProviderFactory;
    private OntologyProvider ontologyProvider;
    //endregion
}
