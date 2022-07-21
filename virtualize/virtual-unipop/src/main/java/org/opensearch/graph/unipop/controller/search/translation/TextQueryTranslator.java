package org.opensearch.graph.unipop.controller.search.translation;


import org.opensearch.graph.unipop.controller.search.AggregationBuilder;
import org.opensearch.graph.unipop.controller.search.QueryBuilder;
import org.opensearch.graph.unipop.controller.utils.CollectionUtil;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.unipop.process.predicate.Text;

import java.util.List;

public class TextQueryTranslator implements PredicateQueryTranslator {
    //region PredicateQueryTranslator Implementation
    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, AggregationBuilder aggregationBuilder, String key, P<?> predicate) {
        Text.TextPredicate text = (Text.TextPredicate)predicate.getBiPredicate();
        switch (text) {
            case PREFIX:
                List<String> prefixes = CollectionUtil.listFromObjectValue(predicate.getValue());
                switch (prefixes.size()) {
                    case 0:
                        break;
                    case 1:
                        queryBuilder.push().prefix(key, prefixes.get(0)).pop();
                        break;
                    default:
                        queryBuilder.bool().should();
                        prefixes.forEach(prefix -> queryBuilder.push().prefix(key, prefix).pop());
                        queryBuilder.pop();
                }
                break;

            case REGEXP:
                List<String> regexs = CollectionUtil.listFromObjectValue(predicate.getValue());
                switch (regexs.size()) {
                    case 0:
                        break;
                    case 1:
                        queryBuilder.push().regexp(key, regexs.get(0)).pop();
                        break;
                    default:
                        queryBuilder.push().bool().should();
                        regexs.forEach(regex ->  queryBuilder.push().regexp(key, regex).pop());
                        queryBuilder.pop();
                }
                break;

            case LIKE:
                if (Iterable.class.isAssignableFrom(predicate.getValue().getClass())) {
                    queryBuilder.push().bool().should();
                    // ((Iterable)predicate.getValue()).forEach(likeValue -> queryBuilder.push().wildcardScript(key, likeValue.toString()).pop());// - ES 5 wildcard script
                    ((Iterable)predicate.getValue()).forEach(likeValue -> queryBuilder.push().wildcard(key, likeValue.toString()).pop());
                    queryBuilder.pop();
                } else {
                    //queryBuilder.push().wildcardScript(key, predicate.getValue().toString()).pop(); // - ES 5 wildcard script
                    queryBuilder.push().wildcard(key, predicate.getValue().toString()).pop();
                }
                break;

            case MATCH:
                if (Iterable.class.isAssignableFrom(predicate.getValue().getClass())) {
                    queryBuilder.push().bool().should();
                    // ((Iterable)predicate.getValue()).forEach(likeValue -> queryBuilder.push().wildcardScript(key, likeValue.toString()).pop());// - ES 5 wildcard script
                    ((Iterable)predicate.getValue()).forEach(likeValue -> queryBuilder.push().match(key, likeValue.toString()).pop());
                    queryBuilder.pop();
                } else {
                    //queryBuilder.push().wildcardScript(key, predicate.getValue().toString()).pop(); // - ES 5 wildcard script
                    queryBuilder.push().match(key, predicate.getValue().toString()).pop();
                }
                break;
            case MATCH_PHRASE:
                if (Iterable.class.isAssignableFrom(predicate.getValue().getClass())) {
                    queryBuilder.push().bool().should();
                    // ((Iterable)predicate.getValue()).forEach(likeValue -> queryBuilder.push().wildcardScript(key, likeValue.toString()).pop());// - ES 5 wildcard script
                    ((Iterable)predicate.getValue()).forEach(likeValue -> queryBuilder.push().matchPhrase(key, likeValue.toString()).pop());
                    queryBuilder.pop();
                } else {
                    //queryBuilder.push().wildcardScript(key, predicate.getValue().toString()).pop(); // - ES 5 wildcard script
                    queryBuilder.push().matchPhrase(key, predicate.getValue().toString()).pop();
                }
                break;

                case QUERY_STRING:
                if (Iterable.class.isAssignableFrom(predicate.getValue().getClass())) {
                    queryBuilder.push().bool().should();
                    // ((Iterable)predicate.getValue()).forEach(likeValue -> queryBuilder.push().wildcardScript(key, likeValue.toString()).pop());// - ES 5 wildcard script
                    ((Iterable)predicate.getValue()).forEach(likeValue -> queryBuilder.push().queryString(key, likeValue.toString()).pop());
                    queryBuilder.pop();
                } else {
                    //queryBuilder.push().wildcardScript(key, predicate.getValue().toString()).pop(); // - ES 5 wildcard script
                    queryBuilder.push().queryString(key, predicate.getValue().toString()).pop();
                }
                break;
        }

        return queryBuilder;
    }

    @Override
    public boolean test(String key, P<?> predicate) {
        return (predicate != null) && (predicate.getBiPredicate() instanceof Text.TextPredicate);
    }
    //endregion
}
