package org.opensearch.graph.executor.ontology.schema;

import com.typesafe.config.Config;
import org.opensearch.graph.dispatcher.driver.IdGeneratorDriver;
import org.opensearch.graph.dispatcher.ontology.IndexProviderFactory;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.executor.ontology.schema.load.CSVTransformer;
import org.opensearch.graph.executor.ontology.schema.load.GraphDataLoader;
import org.opensearch.graph.executor.ontology.schema.load.IndexProviderBasedCSVLoader;
import org.opensearch.graph.model.results.LoadResponse;
import org.opensearch.graph.model.Range;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.resourceInfo.FuseError;
import org.opensearch.graph.model.schema.IndexProvider;
import org.opensearch.graph.test.BaseITMarker;
import org.opensearch.action.admin.indices.refresh.RefreshRequest;
import org.opensearch.action.admin.indices.refresh.RefreshResponse;
import org.opensearch.action.search.SearchRequestBuilder;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.index.query.MatchAllQueryBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.opensearch.graph.executor.TestSuiteIndexProviderSuite.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class IndexProviderBasedCSVLoaderIT implements BaseITMarker {
    private IndexProvider provider;
    private static IndexProviderFactory providerFactory;

    @Before
    public void setUp() throws Exception {

        providerFactory = Mockito.mock(IndexProviderFactory.class);
        when(providerFactory.get(any())).thenAnswer(invocationOnMock -> Optional.of(provider));

        ontologyProvider = Mockito.mock(OntologyProvider.class);
        when(ontologyProvider.get(any())).thenAnswer(invocationOnMock -> Optional.of(ontology));

        config = Mockito.mock(Config.class);
        when(config.getString(any())).thenAnswer(invocationOnMock -> "Dragons");

        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/DragonsIndexProviderNested.conf");
        provider = mapper.readValue(stream, IndexProvider.class);
        stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/Dragons.json");
        ontology = mapper.readValue(stream, Ontology.class);
    }

    @Test
    public void testSchema() throws IOException {
        Set<String> strings = Arrays.asList("idx_fire_500","idx_freeze_2000","idx_fire_1500","idx_freeze_1000","own","subjectof","dragon","idx_freeze_1500","idx_fire_2000","kingdom","people","idx_fire_1000","horse","guild","idx_freeze_500","know","registeredin","originatedin","memberof").stream().collect(Collectors.toSet());
        Assert.assertEquals(strings,StreamSupport.stream(nestedSchema.indices().spliterator(),false).collect(Collectors.toSet()));
    }


    @Test
    public void testLoadDragon() throws IOException, URISyntaxException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(),anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0,1000));

        String[] indices = StreamSupport.stream(nestedSchema.indices().spliterator(), false).map(String::toLowerCase).collect(Collectors.toSet()).toArray(new String[]{});
        CSVTransformer transformer = new CSVTransformer(config, ontologyProvider,nestedProviderIfc, nestedSchema, idGeneratorDriver, client);

        Assert.assertEquals(19,indices.length);

        IndexProviderBasedCSVLoader csvLoader = new IndexProviderBasedCSVLoader(client, transformer, nestedSchema);
        // for stand alone test
//        Assert.assertEquals(19,graphLoader.init());

        URL resource = Thread.currentThread().getContextClassLoader().getResource("schema/csv/Dragons.csv");
        LoadResponse<String, FuseError> response = csvLoader.load("Entity","Dragon",new File(resource.toURI()), GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(2,response.getResponses().size());
        Assert.assertEquals(3,response.getResponses().get(1).getSuccesses().size());

        RefreshResponse actionGet = client.admin().indices().refresh(new RefreshRequest(indices)).actionGet();
        Assert.assertNotNull(actionGet);

        SearchRequestBuilder builder = client.prepareSearch();
        builder.setIndices("dragon");
        SearchResponse resp = builder.setSize(1000).setQuery(new MatchAllQueryBuilder()).get();
        Assert.assertEquals(3,resp.getHits().getTotalHits().value);

    }
    @Test
    public void testLoadFire() throws IOException, URISyntaxException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(),anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0,1000));

        String[] indices = StreamSupport.stream(nestedSchema.indices().spliterator(), false).map(String::toLowerCase).collect(Collectors.toSet()).toArray(new String[]{});
        CSVTransformer transformer = new CSVTransformer(config, ontologyProvider,nestedProviderIfc, nestedSchema, idGeneratorDriver, client);

        Assert.assertEquals(19,indices.length);

        IndexProviderBasedCSVLoader csvLoader = new IndexProviderBasedCSVLoader(client, transformer,nestedSchema);
        // for stand alone test
//        Assert.assertEquals(19,graphLoader.init());

        URL resource  = Thread.currentThread().getContextClassLoader().getResource("schema/csv/Fire.csv");
        LoadResponse<String, FuseError> response = csvLoader.load("Relation","Fire",new File(resource.toURI()), GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(2,response.getResponses().size());
        Assert.assertEquals(4,response.getResponses().get(1).getSuccesses().size());

        RefreshResponse actionGet = client.admin().indices().refresh(new RefreshRequest(indices)).actionGet();
        Assert.assertNotNull(actionGet);

        SearchRequestBuilder builder = client.prepareSearch();
        builder.setIndices("idx_fire*");
        SearchResponse resp = builder.setSize(1000).setQuery(new MatchAllQueryBuilder()).get();
        Assert.assertEquals(4,resp.getHits().getTotalHits().value);

    }
}
