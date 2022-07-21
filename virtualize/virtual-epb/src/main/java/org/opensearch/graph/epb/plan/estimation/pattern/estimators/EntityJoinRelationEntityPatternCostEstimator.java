package org.opensearch.graph.epb.plan.estimation.pattern.estimators;



import org.opensearch.graph.epb.plan.estimation.IncrementalEstimationContext;
import org.opensearch.graph.epb.plan.estimation.pattern.EntityJoinEntityPattern;
import org.opensearch.graph.epb.plan.estimation.pattern.Pattern;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.CountEstimatesCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;

public class EntityJoinRelationEntityPatternCostEstimator implements PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> {
    //region Constructors
    public EntityJoinRelationEntityPatternCostEstimator(
            EntityRelationEntityPatternCostEstimator entityRelationEntityPatternCostEstimator) {
        this.entityRelationEntityPatternCostEstimator = entityRelationEntityPatternCostEstimator;
    }
    //endregion

    //region StepPatternCostEstimator Implementation
    @Override
    public Result<Plan, CountEstimatesCost> estimate(
            Pattern pattern,
            IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery> context) {
        if (!EntityJoinEntityPattern.class.isAssignableFrom(pattern.getClass())) {
            return EmptyResult.get();
        }

        EntityJoinEntityPattern entityJoinEntityPattern = (EntityJoinEntityPattern) pattern;

        Result<Plan, CountEstimatesCost> result =
                this.entityRelationEntityPatternCostEstimator.estimate(entityJoinEntityPattern, context);

        return Result.of(
                result.countsUpdateFactors(),
                context.getPreviousCost().get().getCost().getPlanStepCost(entityJoinEntityPattern.getEntityJoinOp()).get(),
                result.getPlanStepCosts().get(1),
                result.getPlanStepCosts().get(2));
    }
    //endregion

    //region Fields
    private EntityRelationEntityPatternCostEstimator entityRelationEntityPatternCostEstimator;
    //endregion
}
