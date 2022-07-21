package org.opensearch.graph.unipop.controller.discrete;


import com.codahale.metrics.MetricRegistry;
import org.opensearch.graph.unipop.controller.OpensearchGraphConfiguration;
import org.opensearch.graph.unipop.controller.common.appender.*;
import org.opensearch.graph.unipop.controller.common.appender.*;
import org.opensearch.graph.unipop.controller.common.context.CompositeControllerContext;
import org.opensearch.graph.unipop.controller.discrete.appender.DualEdgeDirectionSearchAppender;
import org.opensearch.graph.unipop.controller.discrete.context.DiscreteElementControllerContext;
import org.opensearch.graph.unipop.controller.discrete.context.DiscreteVertexControllerContext;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.unipop.controller.promise.appender.SizeSearchAppender;
import org.opensearch.graph.unipop.controller.search.SearchBuilder;
import org.opensearch.graph.unipop.controller.utils.CollectionUtil;
import org.opensearch.graph.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import org.opensearch.graph.unipop.predicates.SelectP;
import org.opensearch.graph.unipop.promise.Constraint;
import org.opensearch.graph.unipop.promise.TraversalConstraint;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;
import org.opensearch.graph.unipop.structure.ElementType;
import org.opensearch.graph.unipop.structure.discrete.DiscreteVertex;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.T;
import org.opensearch.action.search.SearchRequestBuilder;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.Client;
import org.unipop.process.Profiler;
import org.unipop.query.aggregation.ReduceEdgeQuery;
import org.unipop.query.aggregation.ReduceQuery;
import org.unipop.structure.UniGraph;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.opensearch.graph.unipop.controller.utils.SearchAppenderUtil.wrap;

/**
 * Created by Roman on 3/14/2018.
 */
public class DiscreteElementReduceController implements ReduceQuery.SearchController {
    //region Constructors
    public DiscreteElementReduceController(
            Client client,
            OpensearchGraphConfiguration configuration,
            UniGraph graph,
            GraphElementSchemaProvider schemaProvider,
            MetricRegistry metricRegistry) {

        this.client = client;
        this.configuration = configuration;
        this.graph = graph;
        this.schemaProvider = schemaProvider;
    }
    //endregion

    //region ReduceQuery.SearchController Implementation
    @Override
    public long count(ReduceQuery reduceQuery) {
        SearchBuilder searchBuilder = new SearchBuilder();
        if(reduceQuery instanceof ReduceEdgeQuery){
            buildEdgeQuery((ReduceEdgeQuery) reduceQuery, searchBuilder);
        }else{
            buildVertexQuery(reduceQuery, searchBuilder);
        }

        SearchRequestBuilder searchRequest = searchBuilder.build(client, GlobalConstants.INCLUDE_AGGREGATION);
        SearchResponse response = searchRequest.execute().actionGet();
        long totalHits = response.getHits().getTotalHits().value;
        //report count
        String stepName = "Reduce[" + reduceQuery.getStepDescriptor().getDescription().orElse("?") + "]";
        getProfiler().get().incrementCount(stepName,totalHits);
        return totalHits;

    }

    @Override
    public long max(ReduceQuery uniQuery) {
        throw new UnsupportedOperationException("Please implement me ...");
    }

    @Override
    public long avg(ReduceQuery uniQuery) {
        throw new UnsupportedOperationException("Please implement me ...");
    }

    public long min(ReduceQuery uniQuery) {
        throw new UnsupportedOperationException("Please implement me ...");
    }

    private void buildEdgeQuery(ReduceEdgeQuery reduceQuery, SearchBuilder searchBuilder) {
        Iterable<String> edgeLabels = getRequestedEdgeLabels(reduceQuery.getPredicates().getPredicates());

        List<HasContainer> constraintHasContainers = Stream.ofAll(reduceQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getKey().toLowerCase().equals(GlobalConstants.HasKeys.CONSTRAINT))
                .toJavaList();
        if (constraintHasContainers.size() > 1 ||
                (!constraintHasContainers.isEmpty() && !constraintHasContainers.get(0).getBiPredicate().equals(Compare.eq))) {
            throw new UnsupportedOperationException("Single \"" + GlobalConstants.HasKeys.CONSTRAINT + "\" allowed");
        }


        List<HasContainer> selectPHasContainers = Stream.ofAll(reduceQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() != null)
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() instanceof SelectP)
                .toJavaList();

        Optional<TraversalConstraint> constraint = constraintHasContainers.isEmpty() ?
                Optional.empty() :
                Optional.of((TraversalConstraint) constraintHasContainers.get(0).getValue());

        if (!Stream.ofAll(edgeLabels).isEmpty()) {
            constraint = constraint.isPresent() ?
                    Optional.of(Constraint.by(__.and(__.has(T.label, P.within(Stream.ofAll(edgeLabels).toJavaList())), constraint.get().getTraversal()))) :
                    Optional.of(Constraint.by(__.has(T.label, P.within(Stream.ofAll(edgeLabels).toJavaList()))));
        }

        List<HasContainer> vertexHasContainer = Stream.ofAll(reduceQuery.getVertexPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getKey().toLowerCase().equals(GlobalConstants.HasKeys.CONSTRAINT))
                .toJavaList();
        if(vertexHasContainer.size() > 1){
            throw new UnsupportedOperationException("Single \"" + GlobalConstants.HasKeys.CONSTRAINT + "\" allowed");
        }

        TraversalConstraint vertexTraversalConstraint = (TraversalConstraint)vertexHasContainer.get(0).getValue();
        String vertexLabel = Stream.ofAll(new TraversalValuesByKeyProvider().getValueByKey(vertexTraversalConstraint.getTraversal(), T.label.getAccessor())).get();

        CompositeControllerContext context = new CompositeControllerContext.Impl(
                null,
                new DiscreteVertexControllerContext(
                        this.graph,
                        reduceQuery.getStepDescriptor(),
                        this.schemaProvider,
                        constraint,
                        selectPHasContainers,
                        1,
                        reduceQuery.getDirection(),
                        Collections.singleton(new DiscreteVertex(1,vertexLabel, graph, new HashMap<>()))));

        CompositeSearchAppender<CompositeControllerContext> searchAppender =
                new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all,
                        wrap(new IndexSearchAppender()),
                        wrap(new SizeSearchAppender(this.configuration)),
                        wrap(new ConstraintSearchAppender()),
                        wrap(new FilterSourceSearchAppender()),
                        wrap(new FilterSourceRoutingSearchAppender()),
                        wrap(new ElementRoutingSearchAppender()),
                        wrap(new EdgeSourceSearchAppender()),
//                        wrap(new EdgeSourceCountFilterSearchAppender()),
                        //todo: add configuration to enable/disable routing
//                        wrap(new EdgeRoutingSearchAppender()),
                        wrap(new EdgeSourceRoutingSearchAppender()),
                        wrap(new EdgeIndexSearchAppender()),
                        wrap(new DualEdgeDirectionSearchAppender()),
                        wrap(new MustFetchSourceSearchAppender(GlobalConstants.TYPE)),
                        wrap(new NormalizeRoutingSearchAppender(50)),
                        wrap(new NormalizeIndexSearchAppender(100)));

        searchAppender.append(searchBuilder, context);
    }

    private void buildVertexQuery(ReduceQuery reduceQuery, SearchBuilder searchBuilder) {
        List<HasContainer> constraintHasContainers = Stream.ofAll(reduceQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getKey().toLowerCase().equals(GlobalConstants.HasKeys.CONSTRAINT))
                .toJavaList();
        if (constraintHasContainers.size() > 1 ||
                (!constraintHasContainers.isEmpty() && !constraintHasContainers.get(0).getBiPredicate().equals(Compare.eq))) {
            throw new UnsupportedOperationException("Single \"" + GlobalConstants.HasKeys.CONSTRAINT + "\" allowed");
        }

        List<HasContainer> selectPHasContainers = Stream.ofAll(reduceQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() != null)
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() instanceof SelectP)
                .toJavaList();

        Optional<TraversalConstraint> constraint = constraintHasContainers.isEmpty() ?
                Optional.empty() :
                Optional.of((TraversalConstraint) constraintHasContainers.get(0).getValue());

        CompositeControllerContext context = new CompositeControllerContext.Impl(
                new DiscreteElementControllerContext(this.graph,
                        reduceQuery.getStepDescriptor(),
                        ElementType.vertex,
                        this.schemaProvider,
                        constraint,
                        selectPHasContainers,
                        0),
                null);

        CompositeSearchAppender<CompositeControllerContext> searchAppender =
                new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all,
                        wrap(new ElementIndexSearchAppender()),
                        wrap(new SizeSearchAppender(this.configuration)),
                        wrap(new ConstraintSearchAppender()),
                        wrap(new FilterSourceSearchAppender()),
                        wrap(new FilterSourceRoutingSearchAppender()),
                        wrap(new ElementRoutingSearchAppender()),
                        wrap(new MustFetchSourceSearchAppender(GlobalConstants.TYPE)),
                        wrap(new NormalizeRoutingSearchAppender(50)),
                        wrap(new NormalizeIndexSearchAppender(100)));

        searchAppender.append(searchBuilder, context);
    }

    protected Iterable<String> getRequestedEdgeLabels(Iterable<HasContainer> hasContainers) {
        Optional<HasContainer> labelHasContainer =
                Stream.ofAll(hasContainers)
                        .filter(hasContainer -> hasContainer.getKey().equals(T.label.getAccessor()))
                        .toJavaOptional();

        if (!labelHasContainer.isPresent()) {
            return Collections.emptyList();
        }

        List<String> requestedEdgeLabels = CollectionUtil.listFromObjectValue(labelHasContainer.get().getValue());
        return requestedEdgeLabels;
    }
    //endregion

    //endregion
    @Override
    public Profiler getProfiler() {
        return this.profiler;
    }

    @Override
    public void setProfiler(Profiler profiler) {
        this.profiler = profiler;
    }

    //region Fields
    private Profiler profiler = Profiler.Noop.instance  ;
    private Client client;
    private OpensearchGraphConfiguration configuration;
    private UniGraph graph;
    private GraphElementSchemaProvider schemaProvider;
    private MetricRegistry metricRegistry;
    //endregion
}
