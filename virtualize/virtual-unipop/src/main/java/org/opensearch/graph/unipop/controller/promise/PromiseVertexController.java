package org.opensearch.graph.unipop.controller.promise;





import com.codahale.metrics.MetricRegistry;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.unipop.controller.OpensearchGraphConfiguration;
import org.opensearch.graph.unipop.controller.common.VertexControllerBase;
import org.opensearch.graph.unipop.controller.common.appender.CompositeSearchAppender;
import org.opensearch.graph.unipop.controller.common.context.CompositeControllerContext;
import org.opensearch.graph.unipop.controller.promise.appender.PromiseConstraintSearchAppender;
import org.opensearch.graph.unipop.controller.promise.appender.PromiseEdgeAggregationAppender;
import org.opensearch.graph.unipop.controller.promise.appender.PromiseEdgeIndexAppender;
import org.opensearch.graph.unipop.controller.promise.appender.StartVerticesSearchAppender;
import org.opensearch.graph.unipop.controller.promise.context.PromiseVertexControllerContext;
import org.opensearch.graph.unipop.controller.promise.converter.AggregationPromiseEdgeIterableConverter;
import org.opensearch.graph.unipop.controller.search.SearchBuilder;
import org.opensearch.graph.unipop.controller.utils.idProvider.HashEdgeIdProvider;
import org.opensearch.graph.unipop.controller.utils.labelProvider.PrefixedLabelProvider;
import org.opensearch.graph.unipop.promise.TraversalConstraint;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.opensearch.action.search.SearchRequestBuilder;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.Client;
import org.unipop.query.search.SearchVertexQuery;
import org.unipop.structure.UniGraph;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.opensearch.graph.unipop.controller.utils.SearchAppenderUtil.wrap;

public class PromiseVertexController extends VertexControllerBase {

    //region Constructors
    public PromiseVertexController(Client client, OpensearchGraphConfiguration configuration, UniGraph graph, GraphElementSchemaProvider schemaProvider, MetricRegistry metricRegistry) {
        super(labels -> Stream.ofAll(labels).size() == 1 &&
                Stream.ofAll(labels).get(0).equals(GlobalConstants.Labels.PROMISE));

        this.client = client;
        this.configuration = configuration;
        this.graph = graph;
        this.schemaProvider = schemaProvider;
    }
    //endregion

    //region VertexControllerBase Implementation
    @Override
    protected Iterator<Edge> search(SearchVertexQuery searchVertexQuery, Iterable<String> edgeLabels) {
        if (searchVertexQuery.getVertices().size() == 0) {
            throw new UnsupportedOperationException("SearchVertexQuery must receive a non-empty list of vertices getTo start with");
        }

        List<HasContainer> constraintHasContainers = Stream.ofAll(searchVertexQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getKey()
                        .toLowerCase()
                        .equals(GlobalConstants.HasKeys.CONSTRAINT))
                .toJavaList();

        if (constraintHasContainers.size() > 1) {
            throw new UnsupportedOperationException("Single \"" + GlobalConstants.HasKeys.CONSTRAINT + "\" allowed");
        }

        Optional<TraversalConstraint> constraint = Optional.empty();
        if (constraintHasContainers.size() > 0) {
            constraint = Optional.of((TraversalConstraint) constraintHasContainers.get(0).getValue());
        }

        try {
            return queryPromiseEdges(searchVertexQuery, constraint);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyIterator();
        }
    }
    //endregion

    //region Private Methods
    private Iterator<Edge> queryPromiseEdges(SearchVertexQuery searchVertexQuery, Optional<TraversalConstraint> constraint) throws Exception {
        SearchBuilder searchBuilder = new SearchBuilder();

        CompositeControllerContext context = new CompositeControllerContext.Impl(
                null,
                new PromiseVertexControllerContext(
                        graph,
                        searchVertexQuery.getStepDescriptor(),
                        schemaProvider,
                        constraint,
                        Collections.emptyList(),
                        0,
                        searchVertexQuery.getVertices()));

        CompositeSearchAppender<CompositeControllerContext> compositeAppender =
                new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all,
                        wrap(new StartVerticesSearchAppender()),
                        wrap(new PromiseConstraintSearchAppender()),
                        wrap(new PromiseEdgeAggregationAppender()),
                        wrap(new PromiseEdgeIndexAppender()));

        compositeAppender.append(searchBuilder, context);

        if (searchBuilder.getIndices().size() == 0) {
            //there is no relevant index getTo search...
            return Collections.emptyIterator();
        }

        //search
        SearchRequestBuilder searchRequest = searchBuilder.build(client, GlobalConstants.INCLUDE_AGGREGATION).setSize(0);
        SearchResponse response = searchRequest.execute().actionGet();

        //convert result
        AggregationPromiseEdgeIterableConverter converter = new AggregationPromiseEdgeIterableConverter(
                graph,
                new HashEdgeIdProvider(constraint),
                new PrefixedLabelProvider("_"));

        return Stream.ofAll(converter.convert(response.getAggregations().asMap())).flatMap(edgeIterator -> () -> edgeIterator).iterator();

    }
    //endregion

    //region Fields
    private UniGraph graph;
    private GraphElementSchemaProvider schemaProvider;
    private Client client;
    private OpensearchGraphConfiguration configuration;
    //endregion
}
