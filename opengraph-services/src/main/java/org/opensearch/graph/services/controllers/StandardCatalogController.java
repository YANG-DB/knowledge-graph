package org.opensearch.graph.services.controllers;

/*-
 * #%L
 * opengraph-services
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




import com.cedarsoftware.util.io.JsonWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.model.transport.ContentResponse.Builder;
import org.opensearch.graph.unipop.schemaProviders.GraphEdgeSchema;
import org.opensearch.graph.unipop.schemaProviders.GraphElementConstraint;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;
import org.opensearch.graph.unipop.schemaProviders.GraphVertexSchema;
import javaslang.collection.Stream;

import java.util.List;
import java.util.Optional;

import static org.opensearch.graph.model.transport.Status.*;

/**
 * Created by lior.perry on 19/02/2017.
 */
public class StandardCatalogController implements CatalogController {
    //region Constructors
    @Inject
    public StandardCatalogController(OntologyProvider ontologyProvider,
                                     GraphElementSchemaProviderFactory schemaProviderFactory) {
        this.ontologyProvider = ontologyProvider;
        this.schemaProviderFactory = schemaProviderFactory;
        this.mapper = new ObjectMapper();
    }
    //endregion

    //region CatalogController Implementation

    @Override
    public ContentResponse<List<Ontology>> getOntologies() {
        return Builder.<List<Ontology>>builder(OK, NOT_FOUND)
                .data(Optional.of(Stream.ofAll(this.ontologyProvider.getAll()).toJavaList()))
                .compose();
    }

    @Override
    public ContentResponse<Ontology> getOntology(String id) {
        return Builder.<Ontology>builder(OK, NOT_FOUND)
                .data(ontologyProvider.get(id))
                .compose();
    }

    @Override
    public ContentResponse<Ontology> addOntology(Ontology ontology) {
        return Builder.<Ontology>builder(OK, NOT_FOUND)
                .data(Optional.of(this.ontologyProvider.add(ontology)))
                .compose();
    }

    @Override
    public ContentResponse<String> getSchema(String id) {
        Optional<Ontology> ontology = this.ontologyProvider.get(id);
        if (!ontology.isPresent()) {
            return ContentResponse.notFound();
        }

        GraphElementSchemaProvider schemaProvider = this.schemaProviderFactory.get(this.ontologyProvider.get(id).get());
        return Builder.<String>builder(OK, NOT_FOUND)
                .data(Optional.of(createSerializableSchemaProvider(schemaProvider)))
                .compose();
    }

    @Override
    public ContentResponse<List<String>> getSchemas() {
        return Builder.<List<String>>builder(OK, NOT_FOUND)
                .data(Optional.of(Stream.ofAll(this.ontologyProvider.getAll())
                        .map(ont -> createSerializableSchemaProvider(this.schemaProviderFactory.get(ont)))
                        .toJavaList()))
                .compose();
    }
    //endregion

    //region Private Methods
    private String createSerializableSchemaProvider(GraphElementSchemaProvider schemaProvider)  {
        GraphElementSchemaProvider.Impl value = new GraphElementSchemaProvider.Impl(
                Stream.ofAll(schemaProvider.getVertexSchemas())
                        .map(vertexSchema -> (GraphVertexSchema) new GraphVertexSchema.Impl(
                                vertexSchema.getLabel(),
                                new GraphElementConstraint.Impl(
                                        new TraversalToString(vertexSchema.getConstraint().getTraversalConstraint().toString())),
                                vertexSchema.getRouting(),
                                vertexSchema.getIndexPartitions(),
                                vertexSchema.getProperties()))
                        .toJavaList(),
                Stream.ofAll(schemaProvider.getEdgeSchemas())
                        .map(edgeSchema -> (GraphEdgeSchema) new GraphEdgeSchema.Impl(
                                edgeSchema.getLabel(),
                                new GraphElementConstraint.Impl(
                                        new TraversalToString(edgeSchema.getConstraint().getTraversalConstraint().toString())),
                                edgeSchema.getEndA(),
                                edgeSchema.getEndB(),
                                edgeSchema.getDirection(),
                                edgeSchema.getDirectionSchema(),
                                edgeSchema.getRouting(),
                                edgeSchema.getIndexPartitions(),
                                edgeSchema.getProperties(),
                                edgeSchema.getApplications()))
                        .toJavaList()
        );
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return JsonWriter.objectToJson(value);
        }
    }
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    private ObjectMapper mapper;

    //endregion

    public static class TraversalToString implements org.apache.tinkerpop.gremlin.process.traversal.Traversal {
        //region Constructors
        public TraversalToString(String traversalToString) {
            this.traversal = traversalToString;
        }
        //endregion

        //region Dummy Implementation
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            return null;
        }
        //endregion

        public String getTraversal() {
            return this.traversal;
        }

        private String traversal;
    }
}
