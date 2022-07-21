package org.opensearch.graph.unipop.controller.search.translation;


import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.unipop.controller.search.AggregationBuilder;
import org.opensearch.graph.unipop.controller.search.QueryBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.structure.Graph;

public class HiddenQueryTranslator extends CompositeQueryTranslator {
    //region Constructors
    public HiddenQueryTranslator(Iterable<PredicateQueryTranslator> translators) {
        super(translators);
    }

    public HiddenQueryTranslator(PredicateQueryTranslator...translators) {
        super(translators);
    }
    //endregion

    //region Override Methods
    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, AggregationBuilder aggregationBuilder, String key, P<?> predicate) {
        String plainKey = Graph.Hidden.isHidden(key) ? Graph.Hidden.unHide(key) : key;
        String newKey;

        switch (plainKey) {
            case "id":
                newKey = GlobalConstants._ID;
                break;

            case "label":
                newKey = "type";
                break;

            default:
                newKey = plainKey;
        }

        return super.translate(queryBuilder, aggregationBuilder, newKey, predicate);
    }
    //endregion
}
