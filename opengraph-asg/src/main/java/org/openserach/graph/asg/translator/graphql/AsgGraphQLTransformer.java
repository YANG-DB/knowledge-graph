package org.openserach.graph.asg.translator.graphql;



import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.asg.QueryToAsgTransformer;
import org.opensearch.graph.dispatcher.query.QueryTransformer;
import org.opensearch.graph.dispatcher.query.graphql.GraphQL2QueryTransformer;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.query.QueryInfo;

import java.util.function.Function;

import static org.opensearch.graph.model.transport.CreateQueryRequestMetadata.QueryLanguage.graphql;

public class AsgGraphQLTransformer implements QueryTransformer<QueryInfo<String>, AsgQuery> , Function<QueryInfo<String>,Boolean> {
    public static final String transformerName = "AsgGraphQLTransformer.@transformer";

    private GraphQL2QueryTransformer graphQL2QueryTransformer;
    private final QueryToAsgTransformer queryTransformer;

    //region Constructors
    @Inject
    public AsgGraphQLTransformer(GraphQL2QueryTransformer graphQL2QueryTransformer,
                                 QueryToAsgTransformer queryTransformer) {
        this.graphQL2QueryTransformer = graphQL2QueryTransformer;
        this.queryTransformer = queryTransformer;
    }
    //endregion

    //region QueryTransformer Implementation
    @Override
    public Boolean apply(QueryInfo<String> queryInfo) {
        return graphql.name().equalsIgnoreCase(queryInfo.getQueryType());
    }

    @Override
    public AsgQuery transform(QueryInfo<String> query) {
        Query transform = graphQL2QueryTransformer.transform(query);
        return queryTransformer.transform(transform);
    }

    //endregion

}
