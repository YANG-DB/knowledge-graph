package org.opensearch.graph.epb.plan.estimation.pattern.estimators;


import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.epb.plan.estimation.CostEstimationConfig;
import org.opensearch.graph.epb.plan.estimation.IncrementalEstimationContext;
import org.opensearch.graph.epb.plan.estimation.pattern.*;
import org.opensearch.graph.epb.plan.statistics.StatisticsProviderFactory;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.CountEstimatesCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by moti on 29/05/2017.
 */
public class M2PatternCostEstimator extends CompositePatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> {
    //region Static
    private static Map<Class<? extends Pattern>,
            PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>> estimators(
                    CostEstimationConfig config,
                    StatisticsProviderFactory statisticsProviderFactory,
                    OntologyProvider ontologyProvider,
                    RegexPatternCostEstimator regexPatternCostEstimator) {
        Map<Class<? extends Pattern>, PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>> estimators =
                new HashMap<>();

        estimators.put(EntityPattern.class, new EntityPatternCostEstimator(statisticsProviderFactory, ontologyProvider));
        estimators.put(EntityRelationEntityPattern.class, new EntityRelationEntityPatternCostEstimator(config, statisticsProviderFactory, ontologyProvider));
        estimators.put(GoToEntityRelationEntityPattern.class,
                new GoToEntityRelationEntityPatternCostEstimator(
                        (EntityRelationEntityPatternCostEstimator)estimators.get(EntityRelationEntityPattern.class)));
        EntityJoinPatternCostEstimator entityJoinPatternCostEstimator = new EntityJoinPatternCostEstimator();
        entityJoinPatternCostEstimator.setCostEstimator(regexPatternCostEstimator);
        estimators.put(EntityJoinPattern.class, entityJoinPatternCostEstimator);
        estimators.put(EntityJoinEntityPattern.class, new EntityJoinRelationEntityPatternCostEstimator((EntityRelationEntityPatternCostEstimator)estimators.get(EntityRelationEntityPattern.class)));
        estimators.put(GotoPattern.class, new GotoPatternCostEstimator());

        return estimators;
    }
    //endregion

    //region Constructors
    @Inject
    public M2PatternCostEstimator(
            CostEstimationConfig config,
            StatisticsProviderFactory statisticsProviderFactory,
            OntologyProvider ontologyProvider,
            RegexPatternCostEstimator regexPatternCostEstimator) {
        super(estimators(config, statisticsProviderFactory, ontologyProvider, regexPatternCostEstimator));
    }
    //endregion
}
