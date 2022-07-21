package org.opensearch.graph.unipop.controller.search.translation;



import org.opensearch.graph.unipop.controller.search.AggregationBuilder;
import org.opensearch.graph.unipop.controller.search.QueryBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.unipop.process.predicate.CountFilterP;

import java.util.Collections;

public class CountFilterQueryTranslator implements PredicateQueryTranslator {
    //region PredicateQueryTranslator Implementation
    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, AggregationBuilder aggregationBuilder, String key, P<?> predicate) {
        if (predicate == null) {
            return queryBuilder;
        }

        if (predicate instanceof CountFilterP) {
            CountFilterP filterP = (CountFilterP) predicate;
            String field = key;
            //populate count based filter sub aggregation (bucket-filter type in ES semantics)
            AggregationBuilder countFilter = aggregationBuilder.seek(field).countFilter(field);
            countFilter.field(field);
            countFilter.operator(filterP.getBiPredicate());
            countFilter.operands(Collections.singletonList(filterP.getValue()));
        }


        return queryBuilder;
    }

    @Override
    public boolean test(String key, P<?> predicate) {
        return predicate!=null && predicate.getBiPredicate() instanceof CountFilterP.CountFilterCompare;
    }
    //endregion
}
