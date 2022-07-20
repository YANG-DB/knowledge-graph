package org.opensearch.graph.executor.ontology.schema;

import org.opensearch.graph.dispatcher.driver.IdGeneratorDriver;
import org.opensearch.graph.executor.TestSuiteIndexProviderSuite;
import org.opensearch.graph.executor.ontology.schema.load.EntityTransformer;
import org.opensearch.graph.executor.ontology.schema.load.GraphDataLoader;
import org.opensearch.graph.executor.ontology.schema.load.IndexProviderBasedGraphLoader;
import org.opensearch.graph.model.results.LoadResponse;
import org.opensearch.graph.model.Range;
import org.opensearch.graph.model.logical.LogicalGraphModel;
import org.opensearch.graph.model.resourceInfo.FuseError;
import org.opensearch.graph.test.BaseITMarker;
import org.opensearch.action.admin.indices.refresh.RefreshRequest;
import org.opensearch.action.admin.indices.refresh.RefreshResponse;
import org.opensearch.action.search.SearchRequestBuilder;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.index.query.MatchAllQueryBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class IndexProviderBasedGraphLoaderIT implements BaseITMarker {


    @Test
    public void testSchema() throws IOException {
        Set<String> strings = Arrays.asList("idx_fire_500","idx_freeze_2000","idx_fire_1500","idx_freeze_1000","own","subjectof","dragon","idx_freeze_1500","idx_fire_2000","kingdom","people","idx_fire_1000","horse","guild","idx_freeze_500","know","registeredin","originatedin","memberof").stream().collect(Collectors.toSet());
        Assert.assertEquals(strings,StreamSupport.stream(TestSuiteIndexProviderSuite.nestedSchema.indices().spliterator(),false).collect(Collectors.toSet()));
    }

    @Test
    public void testLoad() throws IOException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(),anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0,1000));

        String[] indices = StreamSupport.stream(TestSuiteIndexProviderSuite.nestedSchema.indices().spliterator(), false).map(String::toLowerCase).collect(Collectors.toSet()).toArray(new String[]{});
        EntityTransformer transformer = new EntityTransformer(TestSuiteIndexProviderSuite.config, TestSuiteIndexProviderSuite.ontologyProvider, TestSuiteIndexProviderSuite.nestedProviderIfc, TestSuiteIndexProviderSuite.nestedSchema, idGeneratorDriver, TestSuiteIndexProviderSuite.client);

        Assert.assertEquals(19,indices.length);

        IndexProviderBasedGraphLoader graphLoader = new IndexProviderBasedGraphLoader(TestSuiteIndexProviderSuite.client, transformer, TestSuiteIndexProviderSuite.nestedSchema, idGeneratorDriver);
        // for stand alone test
//        Assert.assertEquals(19,graphLoader.init());

        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/LogicalDragonsGraph.json");
        LogicalGraphModel graphModel = TestSuiteIndexProviderSuite.mapper.readValue(stream, LogicalGraphModel.class);
        LoadResponse<String, FuseError> response = graphLoader.load(TestSuiteIndexProviderSuite.ontology.getOnt(), graphModel, GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(2,response.getResponses().size());

        Assert.assertEquals(64,response.getResponses().get(0).getSuccesses().size());
        Assert.assertEquals(64,response.getResponses().get(1).getSuccesses().size());

        Assert.assertEquals(0,response.getResponses().get(0).getFailures().size());
        Assert.assertEquals(0,response.getResponses().get(1).getFailures().size());


        RefreshResponse actionGet = TestSuiteIndexProviderSuite.client.admin().indices().refresh(new RefreshRequest(indices)).actionGet();
        Assert.assertNotNull(actionGet);

        SearchRequestBuilder builder = TestSuiteIndexProviderSuite.client.prepareSearch();
        builder.setIndices(indices);
        SearchResponse resp = builder.setSize(1000).setQuery(new MatchAllQueryBuilder()).get();
        Assert.assertEquals(graphModel.getNodes().size() + 2*graphModel.getEdges().size(),resp.getHits().getTotalHits().value);

    }

    @Test
    public void testLoadWithNestedData() throws IOException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(),anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0,1000));

        String[] indices = StreamSupport.stream(TestSuiteIndexProviderSuite.nestedSchema.indices().spliterator(), false).map(String::toLowerCase).collect(Collectors.toSet()).toArray(new String[]{});
        EntityTransformer transformer = new EntityTransformer(TestSuiteIndexProviderSuite.config, TestSuiteIndexProviderSuite.ontologyProvider, TestSuiteIndexProviderSuite.nestedProviderIfc, TestSuiteIndexProviderSuite.nestedSchema, idGeneratorDriver, TestSuiteIndexProviderSuite.client);

        Assert.assertEquals(19,indices.length);

        IndexProviderBasedGraphLoader graphLoader = new IndexProviderBasedGraphLoader(TestSuiteIndexProviderSuite.client, transformer, TestSuiteIndexProviderSuite.nestedSchema, idGeneratorDriver);
        // for stand alone test
//        Assert.assertEquals(19,graphLoader.init());

        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/LogicalDragonsGraphWithNested.json");
        LogicalGraphModel graphModel = TestSuiteIndexProviderSuite.mapper.readValue(stream, LogicalGraphModel.class);
        LoadResponse<String, FuseError> response = graphLoader.load(TestSuiteIndexProviderSuite.ontology.getOnt(), graphModel, GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(2,response.getResponses().size());

        Assert.assertEquals(64,response.getResponses().get(0).getSuccesses().size());
        Assert.assertEquals(64,response.getResponses().get(1).getSuccesses().size());

        Assert.assertEquals(0,response.getResponses().get(0).getFailures().size());
        Assert.assertEquals(0,response.getResponses().get(1).getFailures().size());


        RefreshResponse actionGet = TestSuiteIndexProviderSuite.client.admin().indices().refresh(new RefreshRequest(indices)).actionGet();
        Assert.assertNotNull(actionGet);

        SearchRequestBuilder builder = TestSuiteIndexProviderSuite.client.prepareSearch();
        builder.setIndices(indices);
        SearchResponse resp = builder.setSize(1000).setQuery(new MatchAllQueryBuilder()).get();
        Assert.assertEquals(graphModel.getNodes().size() + 2*graphModel.getEdges().size(),resp.getHits().getTotalHits().value);

    }
}
