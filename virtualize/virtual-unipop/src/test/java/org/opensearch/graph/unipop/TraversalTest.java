package org.opensearch.graph.unipop;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableSet;
import org.opensearch.graph.unipop.controller.OpensearchGraphConfiguration;
import org.opensearch.graph.unipop.controller.common.ElementController;
import org.opensearch.graph.unipop.controller.common.logging.LoggingSearchController;
import org.opensearch.graph.unipop.controller.promise.PromiseElementEdgeController;
import org.opensearch.graph.unipop.controller.promise.PromiseElementVertexController;
import org.opensearch.graph.unipop.controller.search.SearchOrderProvider;
import org.opensearch.graph.unipop.controller.search.SearchOrderProviderFactory;
import org.opensearch.graph.unipop.promise.Constraint;
import org.opensearch.graph.unipop.promise.Promise;
import org.opensearch.graph.unipop.promise.TraversalConstraint;
import org.opensearch.graph.unipop.promise.TraversalPromise;
import org.opensearch.graph.unipop.schemaProviders.EmptyGraphElementSchemaProvider;
import org.opensearch.graph.unipop.structure.SearchUniGraph;
import org.opensearch.graph.unipop.structure.promise.PromiseVertex;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.opensearch.action.search.SearchType;
import org.opensearch.client.Client;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unipop.process.strategyregistrar.StandardStrategyProvider;
import org.unipop.query.controller.ControllerManager;
import org.unipop.query.controller.UniQueryController;
import org.unipop.structure.UniGraph;

import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.mock;

/**
 * Created by lior.perry on 19/03/2017.
 */
public class TraversalTest {
    Client client;
    OpensearchGraphConfiguration configuration;
    SearchOrderProviderFactory orderProvider;
    MetricRegistry metricRegistry;


    @Before
    public void setUp() throws Exception {
        client = mock(Client.class);
        metricRegistry = new MetricRegistry();
        configuration = mock(OpensearchGraphConfiguration.class);
        orderProvider = context -> SearchOrderProvider.of(SearchOrderProvider.EMPTY, SearchType.DEFAULT);

    }

    @Test
    public void g_V_hasXpromise_Promise_asXabcX_byX__hasXlabel_dragonXXX() throws Exception {
        MetricRegistry registry = new MetricRegistry();
        //region ControllerManagerFactory Implementation
        UniGraph graph = new SearchUniGraph(null, graph1 -> new ControllerManager() {
            @Override
            public Set<UniQueryController> getControllers() {
                return ImmutableSet.of(
                        new ElementController(
                                new LoggingSearchController(
                                        new PromiseElementVertexController(client, configuration, graph1, new EmptyGraphElementSchemaProvider(),orderProvider,metricRegistry)
                                        , registry),
                                new LoggingSearchController(
                                        new PromiseElementEdgeController(client, configuration, graph1, new EmptyGraphElementSchemaProvider(), metricRegistry),
                                        registry))
                );
            }

            @Override
            public void close() {

            }
        }, new StandardStrategyProvider());
        GraphTraversalSource g = graph.traversal();

        Traversal dragonTraversal = __.has("label", "dragon");

        Traversal<Vertex, Vertex> traversal = g.V().has("promise", Promise.as("A").by(dragonTraversal));
        Vertex vertex = traversal.next();

        Assert.assertTrue(!traversal.hasNext());
        Assert.assertTrue(vertex.getClass().equals(PromiseVertex.class));

        PromiseVertex promiseVertex = (PromiseVertex) vertex;
        Assert.assertTrue(promiseVertex.id().equals("A"));
        Assert.assertTrue(promiseVertex.label().equals("promise"));
        Assert.assertTrue(promiseVertex.getPromise().getClass().equals(TraversalPromise.class));
        Assert.assertTrue(promiseVertex.getConstraint().equals(Optional.empty()));

        TraversalPromise traversalPromise = (TraversalPromise) promiseVertex.getPromise();
        Assert.assertTrue(traversalPromise.getTraversal().equals(dragonTraversal));
    }

    @Test
    public void g_V_hasXpromise_Promise_asXabcX_byX__hasXlabel_dragonXXX_hasXconstraint_Constraint_byX__hasXlabel_dragonXXX() throws Exception {
        MetricRegistry registry = new MetricRegistry();
        //region ControllerManagerFactory Implementation
        UniGraph graph = new SearchUniGraph(null, graph1 -> new ControllerManager() {
            @Override
            public Set<UniQueryController> getControllers() {
                return ImmutableSet.of(
                        new ElementController(
                                new LoggingSearchController(
                                        new PromiseElementVertexController(client, configuration, graph1, new EmptyGraphElementSchemaProvider(),orderProvider,metricRegistry)
                                        , registry),
                                new LoggingSearchController(
                                        new PromiseElementEdgeController(client, configuration, graph1, new EmptyGraphElementSchemaProvider(), metricRegistry),
                                        registry))
                );
            }

            @Override
            public void close() {

            }
        }, new StandardStrategyProvider());
        GraphTraversalSource g = graph.traversal();

        Traversal dragonTraversal = __.has("label", "dragon");

        Traversal<Vertex, Vertex> traversal = g.V().has("promise", Promise.as("A").by(dragonTraversal)).has("constraint", Constraint.by(dragonTraversal));
        Vertex vertex = traversal.next();

        Assert.assertTrue(!traversal.hasNext());
        Assert.assertTrue(vertex.getClass().equals(PromiseVertex.class));

        PromiseVertex promiseVertex = (PromiseVertex) vertex;
        Assert.assertTrue(promiseVertex.id().equals("A"));
        Assert.assertTrue(promiseVertex.label().equals("promise"));
        Assert.assertTrue(promiseVertex.getPromise().getClass().equals(TraversalPromise.class));
        Assert.assertTrue(promiseVertex.getConstraint().get().getClass().equals(TraversalConstraint.class));

        TraversalPromise traversalPromise = (TraversalPromise) promiseVertex.getPromise();
        Assert.assertTrue(traversalPromise.getTraversal().equals(dragonTraversal));

        TraversalConstraint traversalConstraint = (TraversalConstraint) promiseVertex.getConstraint().get();
        Assert.assertTrue(traversalConstraint.getTraversal().equals(dragonTraversal));
    }
}
