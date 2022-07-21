package org.opensearch.graph.dispatcher.epb;




import org.opensearch.graph.model.execution.plan.PlanWithCost;

/**
 * Created by moti on 2/21/2017.
 */
public interface PlanSearcher<P, C, Q> {
     PlanWithCost<P, C> search(Q query);
}
