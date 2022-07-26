package org.opensearch.graph.unipop.controller.search.translation;





import org.opensearch.graph.unipop.controller.search.AggregationBuilder;
import org.opensearch.graph.unipop.controller.search.QueryBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.util.AndP;

public class NestedQueryTranslator extends CompositeQueryTranslator {
    //region Constructors
    public NestedQueryTranslator(PredicateQueryTranslator...translators) {
        super(translators);
    }

    public NestedQueryTranslator(Iterable<PredicateQueryTranslator> translators) {
        super(translators);
    }
    //endregion

    //region CompositeQueryTranslator Implementation
    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, AggregationBuilder aggregationBuilder, String key, P<?> predicate) {
        AndP<?> andP = (AndP<?>)predicate;
        for(P<?> innerPredicate : andP.getPredicates()) {
            queryBuilder.push();
            queryBuilder = super.translate(queryBuilder, aggregationBuilder, key, innerPredicate);
            queryBuilder.pop();
        }

        return queryBuilder;
    }
    //endregion

    @Override
    public boolean test(String key, P<?> predicate) {
        return ((predicate instanceof AndP<?>));
    }

}
