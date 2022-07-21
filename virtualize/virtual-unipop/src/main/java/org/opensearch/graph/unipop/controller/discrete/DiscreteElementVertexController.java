package org.opensearch.graph.unipop.controller.discrete;


import com.codahale.metrics.MetricRegistry;
import org.opensearch.graph.dispatcher.provision.ScrollProvisioning;
import org.opensearch.graph.unipop.controller.OpensearchGraphConfiguration;
import org.opensearch.graph.unipop.controller.common.appender.*;
import org.opensearch.graph.unipop.controller.common.appender.*;
import org.opensearch.graph.unipop.controller.common.context.CompositeControllerContext;
import org.opensearch.graph.unipop.controller.common.converter.ElementConverter;
import org.opensearch.graph.unipop.controller.discrete.context.DiscreteElementControllerContext;
import org.opensearch.graph.unipop.controller.discrete.converter.DiscreteVertexConverter;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.unipop.controller.promise.appender.SizeSearchAppender;
import org.opensearch.graph.unipop.controller.search.SearchBuilder;
import org.opensearch.graph.unipop.controller.search.SearchOrderProviderFactory;
import org.opensearch.graph.unipop.controller.utils.traversal.TraversalHasStepFinder;
import org.opensearch.graph.unipop.converter.SearchHitScrollIterable;
import org.opensearch.graph.unipop.predicates.SelectP;
import org.opensearch.graph.unipop.promise.TraversalConstraint;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;
import org.opensearch.graph.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.opensearch.action.search.SearchRequestBuilder;
import org.opensearch.client.Client;
import org.opensearch.search.SearchHit;
import org.unipop.process.Profiler;
import org.unipop.process.predicate.DistinctFilterP;
import org.unipop.query.search.SearchQuery;
import org.unipop.structure.UniGraph;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.opensearch.graph.unipop.controller.utils.SearchAppenderUtil.wrap;

public class DiscreteElementVertexController implements SearchQuery.SearchController {
    //region Constructors
    public DiscreteElementVertexController(
            Client client,
            OpensearchGraphConfiguration configuration,
            UniGraph graph,
            GraphElementSchemaProvider schemaProvider,
            SearchOrderProviderFactory orderProviderFactory,
            MetricRegistry metricRegistry) {

        this.client = client;
        this.configuration = configuration;
        this.graph = graph;
        this.orderProviderFactory = orderProviderFactory;
        this.schemaProvider = schemaProvider;
        this.metricRegistry = metricRegistry;

    }
    //endregion

    //region SearchController Implementation
    @Override
    public <E extends Element> Iterator<E> search(SearchQuery<E> searchQuery) {
        List<HasContainer> constraintHasContainers = Stream.ofAll(searchQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getKey().toLowerCase().equals(GlobalConstants.HasKeys.CONSTRAINT))
                .toJavaList();
        if (constraintHasContainers.size() > 1 ||
                (!constraintHasContainers.isEmpty() && !constraintHasContainers.get(0).getBiPredicate().equals(Compare.eq))) {
            throw new UnsupportedOperationException("Single \"" + GlobalConstants.HasKeys.CONSTRAINT + "\" allowed");
        }

        List<HasContainer> selectPHasContainers = Stream.ofAll(searchQuery.getPredicates().getPredicates())
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() != null)
                .filter(hasContainer -> hasContainer.getPredicate().getBiPredicate() instanceof SelectP)
                .toJavaList();

        Optional<TraversalConstraint> constraint = constraintHasContainers.isEmpty() ?
                Optional.empty() :
                Optional.of((TraversalConstraint) constraintHasContainers.get(0).getValue());

        CompositeControllerContext context = new CompositeControllerContext.Impl(
                    new DiscreteElementControllerContext(this.graph,
                            searchQuery.getStepDescriptor(),
                            ElementType.vertex,
                            this.schemaProvider,
                            constraint,
                            selectPHasContainers,
                            searchQuery.getLimit()),
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

        SearchBuilder searchBuilder = new SearchBuilder();
        searchAppender.append(searchBuilder, context);

        SearchRequestBuilder searchRequest = searchBuilder.build(client, GlobalConstants.INCLUDE_AGGREGATION);
        SearchHitScrollIterable searchHits = new SearchHitScrollIterable(
                client,
                new ScrollProvisioning.MetricRegistryScrollProvisioning(metricRegistry,searchQuery.getContext()),
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
        ElementConverter<SearchHit, E> elementConverter = new DiscreteVertexConverter<>(context,profiler);

        javaslang.collection.Iterator<E> results = Stream.ofAll(searchHits)
                .flatMap(elementConverter::convert)
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

    @Override
    public Profiler getProfiler() {
        return this.profiler;
    }

    @Override
    public void setProfiler(Profiler profiler) {
        this.profiler = profiler;
    }
    //endregion

    //region Fields
    private Client client;
    private OpensearchGraphConfiguration configuration;
    private UniGraph graph;
    private SearchOrderProviderFactory orderProviderFactory;
    private GraphElementSchemaProvider schemaProvider;
    private MetricRegistry metricRegistry;
    private Profiler profiler = Profiler.Noop.instance ;
    //endregion
}
