package org.opensearch.graph.dispatcher.epb;





import org.opensearch.graph.model.execution.plan.PlanWithCost;

public interface CostEstimator<P, C, TContext> {
    PlanWithCost<P, C> estimate(P plan, TContext context);

}
