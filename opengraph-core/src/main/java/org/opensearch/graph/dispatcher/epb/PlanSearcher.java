package org.opensearch.graph.dispatcher.epb;




import org.opensearch.graph.model.execution.plan.PlanWithCost;

public interface PlanSearcher<P, C, Q> {
     PlanWithCost<P, C> search(Q query);
}
