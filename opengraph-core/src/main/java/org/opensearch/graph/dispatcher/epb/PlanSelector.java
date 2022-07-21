package org.opensearch.graph.dispatcher.epb;




public interface PlanSelector<P, Q> {
    Iterable<P> select(Q query, Iterable<P> plans);
}
