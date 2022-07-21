package org.opensearch.graph.dispatcher.epb;




import org.opensearch.graph.model.execution.plan.PlanWithCost;

/**
 * Created by moti on 3/27/2017.
 */
public interface CostEstimator<P, C, TContext> {
    PlanWithCost<P, C> estimate(P plan, TContext context);

}
