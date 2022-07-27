package org.opensearch.graph.dispatcher.query.graphql;

/*-
 * #%L
 * opengraph-core
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */





import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.dispatcher.query.QueryTransformer;
import org.opensearch.graph.dispatcher.query.graphql.wiring.TraversalWiringFactory;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.query.QueryInfo;
import org.opensearch.graph.model.resourceInfo.GraphError;
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
            throw new GraphError.GraphErrorException(new GraphError("No ontology was found","Ontology not found "+query.getOntology()));

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
