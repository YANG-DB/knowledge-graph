package org.opensearch.graph.unipop.controller.search.translation;


import org.opensearch.graph.unipop.controller.search.AggregationBuilder;
import org.opensearch.graph.unipop.controller.search.QueryBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.P;

public interface PredicateQueryTranslator {
    QueryBuilder translate(QueryBuilder queryBuilder, AggregationBuilder aggregationBuilder, String key, P<?> predicate);
    boolean test(String key, P<?> predicate);
}
