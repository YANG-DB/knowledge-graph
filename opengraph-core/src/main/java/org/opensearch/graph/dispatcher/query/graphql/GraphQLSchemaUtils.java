package org.opensearch.graph.dispatcher.query.graphql;


import graphql.schema.GraphQLSchema;
import graphql.schema.idl.TypeDefinitionRegistry;

public interface GraphQLSchemaUtils {
    GraphQLSchema getGraphQLSchema();

    TypeDefinitionRegistry getTypeRegistry();
}
