package org.opensearch.graph.unipop.controller.search.translation;





import org.opensearch.graph.unipop.controller.search.AggregationBuilder;
import org.opensearch.graph.unipop.controller.search.QueryBuilder;
import org.opensearch.graph.unipop.controller.utils.CollectionUtil;
import org.apache.tinkerpop.gremlin.process.traversal.Contains;
import org.apache.tinkerpop.gremlin.process.traversal.P;

public class ContainsQueryTranslator implements PredicateQueryTranslator {
    //region PredicateQueryTranslator Implementation
    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, AggregationBuilder aggregationBuilder, String key, P<?> predicate) {
        Contains contains = (Contains) predicate.getBiPredicate();
        switch (contains) {
            case within:
                if (CollectionUtil.listFromObjectValue(predicate.getValue()).isEmpty()) {
                    queryBuilder.push().bool().mustNot().exists(key).pop();
                } else {
                    queryBuilder.push().terms(key, predicate.getValue()).pop();
                }
                break;
            case without:
                if (CollectionUtil.listFromObjectValue(predicate.getValue()).isEmpty()) {
                    queryBuilder.push().exists(key).pop();
                } else {
                    queryBuilder.push().bool().mustNot().terms(key, predicate.getValue()).pop();
                }
                break;
        }

        return queryBuilder;
    }

    @Override
    public boolean test(String key, P<?> predicate) {
        return (predicate != null) && (predicate.getBiPredicate() instanceof Contains);
    }

    //endregion
}
