package org.opensearch.graph.unipop.controller.search.translation;





import org.opensearch.graph.unipop.controller.search.AggregationBuilder;
import org.opensearch.graph.unipop.controller.search.QueryBuilder;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;

import java.util.List;

public class CompositeQueryTranslator implements PredicateQueryTranslator {
    //region Constructors
    public CompositeQueryTranslator(PredicateQueryTranslator...translators) {
        this.translators = Stream.of(translators).toJavaList();
    }

    public CompositeQueryTranslator(Iterable<PredicateQueryTranslator> translators) {
        this.translators = Stream.ofAll(translators).toJavaList();
    }
    //endregion

    //region PredicateQueryTranslator Implementation
    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, AggregationBuilder aggregationBuilder, String key, P<?> predicate) {
        List<PredicateQueryTranslator> translators = Stream.ofAll(this.translators).filter(t -> t.test(key, predicate)).toJavaList();
        for (PredicateQueryTranslator predicateQueryTranslator : translators) {
            queryBuilder = predicateQueryTranslator.translate(queryBuilder, aggregationBuilder, key, predicate);
        }

        return queryBuilder;
    }

    @Override
    public boolean test(String key, P<?> predicate) {
        return true;
    }

    //endregion

    //region Fields
    protected Iterable<PredicateQueryTranslator> translators;
    //endregion
}
