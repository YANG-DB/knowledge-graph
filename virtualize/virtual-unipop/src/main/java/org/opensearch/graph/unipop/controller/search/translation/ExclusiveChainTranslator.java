package org.opensearch.graph.unipop.controller.search.translation;





import org.opensearch.graph.unipop.controller.search.AggregationBuilder;
import org.opensearch.graph.unipop.controller.search.QueryBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.P;

public class ExclusiveChainTranslator extends CompositeQueryTranslator {
    public ExclusiveChainTranslator(PredicateQueryTranslator... translators) {
        super(translators);
    }

    public ExclusiveChainTranslator(Iterable<PredicateQueryTranslator> translators) {
        super(translators);
    }

    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, AggregationBuilder aggregationBuilder, String key, P<?> predicate) {
        for (PredicateQueryTranslator predicateQueryTranslator : translators) {
            if(predicateQueryTranslator.test(key, predicate)) {
                queryBuilder = predicateQueryTranslator.translate(queryBuilder,aggregationBuilder , key, predicate);
                break;
            }
        }

        return queryBuilder;
    }

}
