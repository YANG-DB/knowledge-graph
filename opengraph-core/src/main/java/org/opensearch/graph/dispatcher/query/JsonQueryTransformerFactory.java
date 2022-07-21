package org.opensearch.graph.dispatcher.query;




import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.query.QueryInfo;

public interface JsonQueryTransformerFactory {
    QueryTransformer<QueryInfo<String>, AsgQuery> transform(String type);


}
