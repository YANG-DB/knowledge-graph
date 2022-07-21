package org.opensearch.graph.unipop.controller.search.translation;


import org.opensearch.graph.unipop.controller.search.AggregationBuilder;
import org.opensearch.graph.unipop.controller.search.QueryBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.unipop.process.predicate.CountFilterP;

import java.util.Collections;

/**
 *
 * Example of a terms count query with range filter on amount of terms
 * {
 *   "size": 0,
 *   "query": {
 *     "bool": {
 *       "filter": [
 *         {
 *           "term": {
 *             "direction": "out"
 *           }
 *         }
 *       ]
 *     }
 *   },
 *   "aggs": {
 *     "edges": {
 *       "entityA.id": {
 *         "field": "entityA.id"
 *       },
 *       "aggs": {
 *         "range_bucket_filter": {
 *           "bucket_selector": {
 *             "buckets_path": {
 *               "edgeCount": "_count"
 *             },
 *             "script": "def a=params.edgeCount; a > 405 && a < 567"
 *           }
 *         }
 *       }
 *     }
 *   }
 * }
 */
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
