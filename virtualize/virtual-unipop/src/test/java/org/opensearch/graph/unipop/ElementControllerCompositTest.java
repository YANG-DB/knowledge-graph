package org.opensearch.graph.unipop;

import com.codahale.metrics.MetricRegistry;
import org.opensearch.graph.unipop.controller.OpensearchGraphConfiguration;
import org.opensearch.graph.unipop.controller.common.ElementController;
import org.opensearch.graph.unipop.controller.common.logging.LoggingSearchController;
import org.opensearch.graph.unipop.controller.promise.PromiseElementEdgeController;
import org.opensearch.graph.unipop.controller.promise.PromiseElementVertexController;
import org.opensearch.graph.unipop.controller.search.SearchOrderProvider;
import org.opensearch.graph.unipop.controller.search.SearchOrderProviderFactory;
import org.opensearch.graph.unipop.promise.IdPromise;
import org.opensearch.graph.unipop.schemaProviders.EmptyGraphElementSchemaProvider;
import org.opensearch.graph.unipop.structure.promise.PromiseVertex;
import javaslang.collection.Stream;
import org.apache.lucene.search.TotalHits;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.opensearch.action.ListenableActionFuture;
import org.opensearch.action.search.SearchRequestBuilder;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.search.SearchScrollRequestBuilder;
import org.opensearch.action.search.SearchType;
import org.opensearch.client.Client;
import org.opensearch.common.document.DocumentField;
import org.opensearch.common.text.Text;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.unipop.query.predicates.PredicatesHolder;
import org.unipop.query.search.SearchQuery;
import org.unipop.structure.UniGraph;

import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by lior.perry on 19/03/2017.
 */
public class ElementControllerCompositTest {
    Client client;
    OpensearchGraphConfiguration configuration;
    MetricRegistry registry;

    @Before
    public void setUp() throws Exception {
        registry = new MetricRegistry();
        client = mock(Client.class);
        configuration = mock(OpensearchGraphConfiguration.class);
        when(configuration.getElasticGraphScrollSize()).thenReturn(1000);
        when(configuration.getElasticGraphScrollTime()).thenReturn(1000);
        when(configuration.getElasticGraphMaxSearchSize()).thenReturn(1000L);

        SearchResponse searchResponse = mock(SearchResponse.class);
        ListenableActionFuture actionFuture = mock(ListenableActionFuture.class);
        SearchRequestBuilder requestBuilder = mock(SearchRequestBuilder.class);
        SearchScrollRequestBuilder scrollRequestBuilder = mock(SearchScrollRequestBuilder.class);
        //client
        when(client.prepareSearchScroll(anyString())).thenReturn(scrollRequestBuilder);
        when(client.prepareSearch()).thenReturn(requestBuilder);
        when(client.prepareSearch(Matchers.any(String[].class))).then(invocationOnMock -> requestBuilder);
        when(client.search(Matchers.any())).thenReturn(actionFuture);
        when(client.searchScroll(Matchers.any())).thenReturn(actionFuture);

        //SearchRequestBuilder
        when(requestBuilder.setScroll(Matchers.any(TimeValue.class))).thenReturn(requestBuilder);
        when(requestBuilder.setSize(anyInt())).thenReturn(requestBuilder);
        when(requestBuilder.setSearchType(Matchers.any(SearchType.class))).thenReturn(requestBuilder);
        when(requestBuilder.addSort(anyString(), any())).thenReturn(requestBuilder);
        when(requestBuilder.execute()).thenReturn(actionFuture);

        //scrollRequestBuilder
        when(scrollRequestBuilder.setScroll(Matchers.any(TimeValue.class))).thenReturn(scrollRequestBuilder);
        when(scrollRequestBuilder.execute()).thenReturn(actionFuture);

        //ListenableActionFuture
        when(actionFuture.actionGet()).thenReturn(searchResponse);
        //searchResponse
        when(searchResponse.getScrollId()).thenReturn("a");

        //search hits
        Map<String, DocumentField> fields = new HashMap<>();
        fields.put("name", new DocumentField("name", Collections.singletonList("myName")));
        fields.put("type", new DocumentField("type", Collections.singletonList("myType")));
        SearchHit[] tests = new SearchHit[]{new SearchHit(1, "1", new Text("test"), fields,null)};

        SearchHits searchHits = new SearchHits(tests, new TotalHits(10, TotalHits.Relation.GREATER_THAN_OR_EQUAL_TO), 1.0f);
        when(searchResponse.getHits()).thenReturn(searchHits);
    }

    @Test
    @Ignore
    public void testSingleIdPromiseVertexWithLimit() {
        UniGraph graph = mock(UniGraph.class);
        SearchOrderProviderFactory orderProvider = context -> {
            return SearchOrderProvider.of(SearchOrderProvider.EMPTY, SearchType.DEFAULT);
        };
        MetricRegistry metricRegistry = new MetricRegistry();
        PredicatesHolder predicatesHolder = mock(PredicatesHolder.class);
        when(predicatesHolder.getPredicates()).thenReturn(Collections.emptyList());

        SearchQuery searchQuery = mock(SearchQuery.class);
        when(searchQuery.getLimit()).thenReturn(10);
        when(searchQuery.getReturnType()).thenReturn(Vertex.class);
        when(searchQuery.getPredicates()).thenReturn(predicatesHolder);

        SearchQuery.SearchController elementController =
                new ElementController(
                        new LoggingSearchController(
                                new PromiseElementVertexController(client, configuration, graph, new EmptyGraphElementSchemaProvider(),orderProvider,metricRegistry)
                                , registry),
                        new LoggingSearchController(
                                new PromiseElementEdgeController(client, configuration, graph, new EmptyGraphElementSchemaProvider(), metricRegistry),
                                registry));
        List<Vertex> vertices = Stream.ofAll(() -> (Iterator<Vertex>) elementController.search(searchQuery)).toJavaList();

        Assert.assertTrue(vertices.size() == 10);
        Assert.assertTrue(vertices.get(0).id().equals("1"));
        Assert.assertTrue(vertices.get(0).label().equals("promise"));
        Assert.assertTrue(vertices.get(0).getClass().equals(PromiseVertex.class));

        PromiseVertex promiseVertex = (PromiseVertex) vertices.get(0);
        Assert.assertTrue(promiseVertex.getPromise().getId().equals("1"));
        Assert.assertTrue(promiseVertex.getPromise().getClass().equals(IdPromise.class));
    }
}
