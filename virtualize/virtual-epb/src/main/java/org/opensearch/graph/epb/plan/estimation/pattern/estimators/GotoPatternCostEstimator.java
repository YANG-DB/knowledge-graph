package org.opensearch.graph.epb.plan.estimation.pattern.estimators;


import org.opensearch.graph.epb.plan.estimation.IncrementalEstimationContext;
import org.opensearch.graph.epb.plan.estimation.pattern.GotoPattern;
import org.opensearch.graph.epb.plan.estimation.pattern.Pattern;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.CountEstimatesCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;

public class GotoPatternCostEstimator implements PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> {
    @Override
    public Result<Plan, CountEstimatesCost> estimate(Pattern pattern, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery> context) {
        GotoPattern gotoPattern = (GotoPattern) pattern;

        PlanWithCost<Plan, CountEstimatesCost> entityOpCost = context.getPreviousCost().get().getCost().getPlanStepCost(gotoPattern.getEntityOp()).get();
        CountEstimatesCost gotoCost = new CountEstimatesCost(0, entityOpCost.getCost().peek());

        return Result.of(new double[]{1}, new PlanWithCost<>(new Plan(gotoPattern.getGoToEntityOp()), gotoCost));
    }
}
