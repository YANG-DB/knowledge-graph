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




import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.driver.DashboardDriver;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.opensearch.graph.executor.resource.PersistantNodeStatusResource;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.resourceInfo.GraphError;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.IndexPartitions;
import org.apache.commons.collections4.IteratorUtils;
import org.opensearch.action.search.SearchRequestBuilder;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.Client;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.opensearch.search.aggregations.bucket.histogram.InternalDateHistogram;
import org.opensearch.search.aggregations.bucket.terms.StringTerms;
import org.opensearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.opensearch.graph.executor.ontology.schema.GraphElementSchemaProviderJsonFactory.IN;
import static org.opensearch.graph.model.GlobalConstants.DEFAULT_DATE_FORMAT;
import static org.opensearch.graph.model.GlobalConstants.EdgeSchema.*;
import static org.opensearch.graph.model.GlobalConstants.TYPE;
import static org.opensearch.graph.model.ontology.Ontology.Accessor.NodeType.ENTITY;
import static org.opensearch.graph.model.ontology.Ontology.Accessor.NodeType.RELATION;
import static org.opensearch.index.query.QueryBuilders.*;

/**
 * Created by lior.perry on 20/02/2017.
 */
public class StandardDashboardDriver implements DashboardDriver {
    private Logger logger = LoggerFactory.getLogger(StandardDashboardController.class);

    public static final String CREATION_TIME = "creationTime";
    public static ObjectMapper objectMapper = new ObjectMapper();

    private Client client;
    private final OntologyProvider ontologyProvider;
    private final GraphElementSchemaProviderFactory schemaProviderFactory;

    //region Constructors
    @Inject
    public StandardDashboardDriver(Client client, OntologyProvider ontologyProvider, GraphElementSchemaProviderFactory schemaProviderFactory) {
        this.client = client;
        this.ontologyProvider = ontologyProvider;
        this.schemaProviderFactory = schemaProviderFactory;
    }

    @Override
    //todo - fix this to be Ontology depended
    /**
     * count the cardinality of the vertices
     * Count the cardinality of edges where each edge will reflect the amount of different relationship types
     * for example if relationship OWN has the following types :
     *  - Person-[Own]-Dragons
     *  - Person-[Own]-Horse
     *  The result will reflect different amount of these types of relationships
     *
     *
     *  The needed aggregation
     *  {
     *   "size": 0,
     *   "query": {
     *     "bool": {
     *       "filter": {
     *         "term": {
     *           "direction": "out"
     *         }
     *       }
     *     }
     *   },
     *   "aggs": {
     *     "typeA_types": {
     *       "terms": {
     *         "field": "entityA.type"
     *       }
     *     },
     *     "typeB_count": {
     *       "terms": {
     *         "field": "entityB.type"
     *       }
     *     }
     *   }
     * }
     */
    public ObjectNode graphElementCount(String ontologyId) {
        Ontology ontology = ontologyProvider.get(ontologyId)
                .orElseThrow(() -> new GraphError.GraphErrorException(new GraphError("No Ontology present for Id ", "No Ontology present for id[" + ontologyId + "]")));

        ObjectNode root = objectMapper.createObjectNode();
        schemaProviderFactory.get(ontology).getVertexSchemas().forEach(vertex -> {
            if (vertex.getIndexPartitions().isPresent()) {
                ObjectNode node = objectMapper.createObjectNode();
                collectIndexMetadata(node, true, vertex.getIndexPartitions().get());
                root.put(vertex.getLabel(), node);
            }
        });
        schemaProviderFactory.get(ontology).getEdgeSchemas().forEach(edgeSchema -> {
            if (edgeSchema.getIndexPartitions().isPresent()) {
                ObjectNode node = objectMapper.createObjectNode();
                collectIndexMetadata(node, false, edgeSchema.getIndexPartitions().get());
                root.put(edgeSchema.getLabel(), node);
            }
        });


        return root;
    }

    private ObjectNode collectIndexMetadata(ObjectNode statistics, boolean vertex, IndexPartitions indexPartitions) {
        final SearchRequestBuilder builder = client.prepareSearch();
        builder.setSize(0);
        builder.setIndices(IteratorUtils.toList(indexPartitions.getIndices().iterator()).toArray(new String[0]));
        if (vertex) {
            builder.addAggregation(new TermsAggregationBuilder("graphElementCount").field(TYPE));
        } else {
            builder.setQuery(QueryBuilders.boolQuery().filter(QueryBuilders.termQuery(DIRECTION, IN)));//only outgoing edges
            builder.addAggregation(new TermsAggregationBuilder("edgeSourceCount").field(SOURCE_TYPE));
            builder.addAggregation(new TermsAggregationBuilder("edgeDestCount").field(DEST_TYPE));
        }
        try {
            final SearchResponse response = builder.get();
            Map<Object, Long> elementCount = Collections.EMPTY_MAP;
            Map<Object, Long> edgeSourceCount = Collections.EMPTY_MAP;
            Map<Object, Long> edgeDestCount = Collections.EMPTY_MAP;

            //calculate cardinality for every entity type
            try {
                elementCount = Objects.isNull(response.getAggregations().get("graphElementCount")) ? Collections.EMPTY_MAP :
                        ((StringTerms) response.getAggregations().get("graphElementCount")).getBuckets().stream()
                                .collect(Collectors.toMap(StringTerms.Bucket::getKey, StringTerms.Bucket::getDocCount));
            } catch (Throwable graphElementCountErr) {
                logger.warn(graphElementCountErr.getMessage());
            }

            //calculate cardinality for every relationship type
            try {
                edgeSourceCount = Objects.isNull(response.getAggregations().get("edgeSourceCount")) ? Collections.EMPTY_MAP :
                        ((StringTerms) response.getAggregations().get("edgeSourceCount")).getBuckets().stream()
                                .collect(Collectors.toMap(StringTerms.Bucket::getKey, StringTerms.Bucket::getDocCount));
            }catch (Throwable edgeSourceCountErr) {
                logger.warn(edgeSourceCountErr.getMessage());
            }

            try {
                edgeDestCount = Objects.isNull(response.getAggregations().get("edgeDestCount")) ? Collections.EMPTY_MAP :
                        ((StringTerms) response.getAggregations().get("edgeDestCount")).getBuckets().stream()
                                .collect(Collectors.toMap(StringTerms.Bucket::getKey, StringTerms.Bucket::getDocCount));
            }catch (Throwable edgeDestCountErr) {
                logger.warn(edgeDestCountErr.getMessage());
            }

            if (!elementCount.isEmpty()) {
                elementCount.forEach((key, value) -> statistics.put(key.toString(), value));
            }

            if (!edgeSourceCount.isEmpty()) {
                edgeSourceCount.forEach((key, value) -> statistics.put(key.toString(), value));
            }

            if (!edgeDestCount.isEmpty()) {
                edgeDestCount.forEach((key, value) -> statistics.put(key.toString(), value));
            }
        } catch (Throwable err) {
            //log error
            logger.warn("Failed fetching index info for storage", err);
        }

        return statistics;
    }

    @Override
    public ObjectNode graphElementCreated(String ontologyId) {
        Ontology ontology = ontologyProvider.get(ontologyId)
                .orElseThrow(() -> new GraphError.GraphErrorException(new GraphError("No Ontology present for Id ", "No Ontology present for id[" + ontologyId + "]")));

        final SearchRequestBuilder builder = client.prepareSearch();
        builder.setSize(0);
        builder.setQuery(boolQuery()
                .should(termQuery(TYPE, ENTITY.toString().toLowerCase()))
                .should(termQuery(TYPE, RELATION.toString().toLowerCase())));
        final DateHistogramAggregationBuilder aggregation = new DateHistogramAggregationBuilder("graphElementCreatedOverTime");
        aggregation.field(CREATION_TIME);
        aggregation.interval(1000 * 60 * 60 * 24);
        aggregation.format(DEFAULT_DATE_FORMAT);
        final SearchResponse response = builder.addAggregation(aggregation).get();
        final Map<Object, Long> elementCount = ((InternalDateHistogram) response.getAggregations().get("graphElementCreatedOverTime")).getBuckets().stream()
                .collect(Collectors.toMap(InternalDateHistogram.Bucket::getKey, InternalDateHistogram.Bucket::getDocCount));

        ObjectNode root = objectMapper.createObjectNode();
        elementCount.forEach((key, value) -> root.put(key.toString(), value));
        return root;
    }

    @Override
    public ObjectNode cursorCount() {
        ObjectNode node = objectMapper.createObjectNode();
        final SearchRequestBuilder builder = client.prepareSearch();
        builder.setIndices(PersistantNodeStatusResource.SYSTEM);
        builder.setQuery(matchAllQuery());
        builder.setSize(0);
        SearchResponse response = builder.get();
        return node.put("count", response.getHits().getTotalHits().value);
    }

    //enStridregion
}
