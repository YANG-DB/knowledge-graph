package org.opensearch.graph.dispatcher.query.graphql;


import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.dispatcher.query.QueryTransformer;
import org.opensearch.graph.dispatcher.query.graphql.wiring.TraversalWiringFactory;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.query.QueryInfo;
import org.opensearch.graph.model.resourceInfo.FuseError;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;

import java.util.Optional;

public class GraphQL2QueryTransformer implements QueryTransformer<QueryInfo<String>, Query> {

    private final GraphQLSchemaUtils schemaUtils;
    private OntologyProvider ontologyProvider;

    @Inject
    public GraphQL2QueryTransformer(GraphQLSchemaUtils schemaUtils, OntologyProvider ontologyProvider) {
        this.schemaUtils = schemaUtils;
        this.ontologyProvider = ontologyProvider;
    }


    public Query transform(QueryInfo<String> query) {
        Optional<Ontology> ontology =  ontologyProvider.get(query.getOntology());
        if(!ontology.isPresent())
            throw new FuseError.FuseErrorException(new FuseError("No ontology was found","Ontology not found "+query.getOntology()));

        return transform(schemaUtils,ontology.get(),query.getQuery());
    }

    public static Query transform(GraphQLSchemaUtils schemaUtils,Ontology ontology, String query) {
        Query.Builder instance = Query.Builder.instance();
        GraphQLSchema schema = createSchema(schemaUtils,ontology,instance);
        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionResult execute = graphQL.execute(query);
        if(execute.getErrors().isEmpty())
            return instance.build();

        //todo implement projection fields
//        query.setProjectedFields(populate);

        throw new IllegalArgumentException(execute.getErrors().toString());
    }

    private static GraphQLSchema createSchema(GraphQLSchemaUtils schemaUtils,Ontology ontology,Query.Builder builder) {
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(
                schemaUtils.getTypeRegistry(),
                TraversalWiringFactory.newEchoingWiring(schemaUtils,ontology,builder));
    }

}
