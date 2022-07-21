package org.opensearch.graph.epb.plan.estimation.dummy;


import org.opensearch.graph.dispatcher.epb.CostEstimator;
import org.opensearch.graph.model.execution.plan.PlanWithCost;

public class DummyCostEstimator<P, C, TContext> implements CostEstimator<P, C, TContext> {
    public DummyCostEstimator(C dummyCost) {
        this.dummyCost = dummyCost;
    }


    private C dummyCost;

    @Override
    public PlanWithCost<P, C> estimate(P plan, TContext context) {
        return new PlanWithCost<>(plan, dummyCost);
    }
}
