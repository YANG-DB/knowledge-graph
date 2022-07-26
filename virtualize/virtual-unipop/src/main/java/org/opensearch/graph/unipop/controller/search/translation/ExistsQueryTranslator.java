package org.opensearch.graph.unipop.controller.search.translation;





import org.opensearch.graph.unipop.controller.search.AggregationBuilder;
import org.opensearch.graph.unipop.controller.search.QueryBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.unipop.process.predicate.ExistsP;
import org.unipop.process.predicate.NotExistsP;

public class ExistsQueryTranslator implements PredicateQueryTranslator {
    //region PredicateQueryTranslator Implementation
    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, AggregationBuilder aggregationBuilder, String key, P<?> predicate) {
        if (predicate == null) {
            return queryBuilder;
        }

        if (predicate instanceof ExistsP) {
            return queryBuilder.push().exists(key).pop();
        }

        if (predicate instanceof NotExistsP) {
            return queryBuilder.push().bool().mustNot().exists(key).pop();
        }

        return queryBuilder;
    }

    @Override
    public boolean test(String key, P<?> predicate) {
        return predicate!=null;
    }
    //endregion
}
