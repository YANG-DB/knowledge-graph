package org.opensearch.graph.unipop.controller.discrete;

/*-
 * #%L
 * fuse-dv-unipop
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
import com.codahale.metrics.MetricRegistry;
import org.opensearch.graph.dispatcher.provision.ScrollProvisioning;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.unipop.controller.OpensearchGraphConfiguration;
import org.opensearch.graph.unipop.controller.common.VertexControllerBase;
import org.opensearch.graph.unipop.controller.common.appender.*;
import org.opensearch.graph.unipop.controller.common.appender.*;
import org.opensearch.graph.unipop.controller.common.context.CompositeControllerContext;
import org.opensearch.graph.unipop.controller.common.converter.ElementConverter;
import org.opensearch.graph.unipop.controller.discrete.context.DiscreteVertexFilterControllerContext;
import org.opensearch.graph.unipop.controller.discrete.converter.DiscreteVertexFilterConverter;
import org.opensearch.graph.unipop.controller.promise.appender.SizeSearchAppender;
import org.opensearch.graph.unipop.controller.search.SearchBuilder;
import org.opensearch.graph.unipop.controller.search.SearchOrderProviderFactory;
import org.opensearch.graph.unipop.controller.utils.traversal.TraversalHasStepFinder;
import org.opensearch.graph.unipop.converter.SearchHitScrollIterable;
import org.opensearch.graph.unipop.predicates.SelectP;
import org.opensearch.graph.unipop.promise.TraversalConstraint;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.opensearch.action.search.SearchRequestBuilder;
import org.opensearch.client.Client;
import org.opensearch.search.SearchHit;
import org.unipop.process.predicate.DistinctFilterP;
import org.unipop.query.search.SearchVertexQuery;
import org.unipop.structure.UniGraph;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.opensearch.graph.unipop.controller.utils.SearchAppenderUtil.wrap;

/**
 * Created by roman.margolis on 26/09/2017.
 * This Search controller is responsible for the edge->vertex (including filter) type documents search (arriving from edge to vertex) pushed down (including predicates) to the engine...
 */
public class DiscreteVertexFilterController extends VertexControllerBase {
    //region Constructors
    public DiscreteVertexFilterController(Client client, OpensearchGraphConfiguration configuration, UniGraph graph, GraphElementSchemaProvider schemaProvider, SearchOrderProviderFactory orderProviderFactory, MetricRegistry metricRegistry) {
        super(labels -> Stream.ofAll(labels).size() == 1 &&
                Stream.ofAll(labels).get(0).equals(GlobalConstants.Labels.PROMISE_FILTER));

        this.client = client;
        this.configuration = configuration;
        this.graph = graph;
        this.schemaProvider = schemaProvider;
        this.orderProviderFactory = orderProviderFactory;
        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region VertexControllerBase Implementation
    @Override
    protected Iterator<Edge> search(SearchVertexQuery searchVertexQuery, Iterable<String> edgeLabels) {
        if (searchVertexQuery.getVertices().size() == 0){
            throw new UnsupportedOperationException("SearchVertexQuery must receive a non-empty list of vertices getTo start with");
        }

        List<HasContainer> constraintHasContainers = Stream.ofAll(searchVertexQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getKey().toLowerCase().equals(GlobalConstants.HasKeys.CONSTRAINT))
                .toJavaList();
        if (constraintHasContainers.size() > 1){
            throw new UnsupportedOperationException("Single \"" + GlobalConstants.HasKeys.CONSTRAINT + "\" allowed");
        }

        Optional<TraversalConstraint> constraint = Optional.empty();
        if(constraintHasContainers.size() > 0) {
            constraint = Optional.of((TraversalConstraint) constraintHasContainers.get(0).getValue());
        }

        List<HasContainer> selectPHasContainers = Stream.ofAll(searchVertexQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() != null)
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() instanceof SelectP)
                .toJavaList();

        return filterVertices(searchVertexQuery, constraint, selectPHasContainers);
    }
    //endregion

    //region Private Methods
    private Iterator<Edge> filterVertices(
            SearchVertexQuery searchVertexQuery,
            Optional<TraversalConstraint> constraint,
            List<HasContainer> selectPHasContainers) {
        SearchBuilder searchBuilder = new SearchBuilder();

        CompositeControllerContext context = new CompositeControllerContext.Impl(
                null,
                new DiscreteVertexFilterControllerContext(
                        this.graph,
                        searchVertexQuery.getStepDescriptor(),
                        searchVertexQuery.getVertices(),
                        constraint,
                        selectPHasContainers,
                        schemaProvider,
                        searchVertexQuery.getLimit()));

        CompositeSearchAppender<CompositeControllerContext> appender =
                new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all,
                        wrap(new SizeSearchAppender(configuration)),
                        wrap(new ConstraintSearchAppender()),
                        wrap(new ElementRoutingSearchAppender()),
                        wrap(new FilterBulkSearchAppender()),
                        wrap(new FilterSourceSearchAppender()),
                        wrap(new FilterSourceRoutingSearchAppender()),
                        wrap(new FilterRoutingSearchAppender()),
                        wrap(new FilterIndexSearchAppender()),
                        wrap(new MustFetchSourceSearchAppender(GlobalConstants.TYPE)),
                        wrap(new NormalizeRoutingSearchAppender(50)),
                        wrap(new NormalizeIndexSearchAppender(100)));

        appender.append(searchBuilder, context);

        SearchRequestBuilder searchRequest = searchBuilder.build(client, GlobalConstants.INCLUDE_AGGREGATION);

        SearchHitScrollIterable searchHits = new SearchHitScrollIterable(
                client,
                new ScrollProvisioning.MetricRegistryScrollProvisioning(metricRegistry,searchVertexQuery.getContext()),
                searchRequest,
                orderProviderFactory.build(context),
                searchBuilder.getLimit(),
                searchBuilder.getScrollSize(),
                searchBuilder.getScrollTime()
        );

        //log step controller query
        context.getStepDescriptor().getDescription().ifPresent(v->
                profiler.get().setAnnotation(v,searchRequest.toString()));
        //convert hits to elements
        ElementConverter<SearchHit, Edge> converter = new DiscreteVertexFilterConverter(context,profiler);

        javaslang.collection.Iterator<Edge> results = Stream.ofAll(searchHits)
                .flatMap(converter::convert)
                .filter(Objects::nonNull).iterator();

        //dedupe for distinct operator
        if (context.getConstraint().isPresent()) {
            if (!Stream.ofAll(new TraversalHasStepFinder(DistinctFilterP::hasDistinct)
                    .getValue(context.getConstraint().get().getTraversal()))
                    .toJavaSet().isEmpty()) {
                results = results.distinct();
            }
        }
        return results;
    }
    //endregion


    //region Fields

    private UniGraph graph;
    private GraphElementSchemaProvider schemaProvider;
    private SearchOrderProviderFactory orderProviderFactory;
    private MetricRegistry metricRegistry;
    private Client client;
    private OpensearchGraphConfiguration configuration;
    //endregion
}
