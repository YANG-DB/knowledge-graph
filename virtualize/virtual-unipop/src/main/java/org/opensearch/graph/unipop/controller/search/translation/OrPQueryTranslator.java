package org.opensearch.graph.unipop.controller.search.translation;





import org.opensearch.graph.unipop.controller.search.AggregationBuilder;
import org.opensearch.graph.unipop.controller.search.QueryBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.util.OrP;

public class OrPQueryTranslator extends CompositeQueryTranslator {
    //region Constructors
    public OrPQueryTranslator(PredicateQueryTranslator...translators) {
        super(translators);
    }

    public OrPQueryTranslator(Iterable<PredicateQueryTranslator> translators) {
        super(translators);
    }    //endregion

    //region CompositeQueryTranslator Implementation
    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, AggregationBuilder aggregationBuilder, String key, P<?> predicate) {
        OrP<?> orP = (OrP<?>)predicate;
        queryBuilder.push().bool().should();
        for(P<?> innerPredicate : orP.getPredicates()) {
            queryBuilder.push();
            queryBuilder = super.translate(queryBuilder, aggregationBuilder, key, innerPredicate);
            queryBuilder.pop();
        }

        return queryBuilder.pop();
    }
    //endregion

    @Override
    public boolean test(String key, P<?> predicate) {
        return (predicate instanceof OrP<?>);
    }
}
