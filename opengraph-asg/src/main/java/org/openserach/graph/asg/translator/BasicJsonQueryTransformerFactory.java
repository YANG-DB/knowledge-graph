package org.openserach.graph.asg.translator;



import com.google.inject.Inject;
import org.openserach.graph.asg.translator.cypher.AsgCypherTransformer;
import org.openserach.graph.asg.translator.graphql.AsgGraphQLTransformer;
import org.openserach.graph.asg.translator.sparql.AsgSparQLTransformer;
import org.opensearch.graph.dispatcher.query.JsonQueryTransformerFactory;
import org.opensearch.graph.dispatcher.query.QueryTransformer;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.query.QueryInfo;
import org.opensearch.graph.model.resourceInfo.FuseError;
import org.opensearch.graph.model.transport.CreateQueryRequestMetadata;

public class BasicJsonQueryTransformerFactory implements JsonQueryTransformerFactory {

    private AsgCypherTransformer cypherTransformer;
    private AsgGraphQLTransformer graphQueryTransformer;
    private AsgSparQLTransformer sparQLTransformer;

    @Inject
    public BasicJsonQueryTransformerFactory(AsgCypherTransformer cypherTransformer,
                                            AsgSparQLTransformer sparQLTransformer,
                                            AsgGraphQLTransformer graphQueryTransformer) {
        this.cypherTransformer = cypherTransformer;
        this.sparQLTransformer = sparQLTransformer;
        this.graphQueryTransformer = graphQueryTransformer;
    }


    @Override
    public QueryTransformer<QueryInfo<String>, AsgQuery> transform(String type) {
        switch (type) {
            case CreateQueryRequestMetadata.TYPE_SPARQL:
                return sparQLTransformer;
            case CreateQueryRequestMetadata.TYPE_CYPHERQL:
                return cypherTransformer;
            case CreateQueryRequestMetadata.TYPE_GRAPHQL:
                return graphQueryTransformer;
        }
        throw new FuseError.FuseErrorException(new FuseError("No Query translator found","No matching json query translator found for type "+type));
    }


}
