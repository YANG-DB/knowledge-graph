package org.opensearch.graph.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import org.junit.Ignore;
import org.opensearch.graph.dispatcher.ontology.IndexProviderFactory;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.executor.opensearch.OpensearchIndexProviderMappingFactoryIT;
import org.opensearch.graph.executor.ontology.schema.*;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.schema.IndexProvider;
import org.opensearch.graph.test.framework.index.SearchEmbeddedNode;
import org.opensearch.graph.test.framework.index.GlobalSearchEmbeddedNode;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.IndexPartitions;
import org.opensearch.graph.test.BaseSuiteMarker;
import org.opensearch.client.Client;
import org.jooby.Jooby;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.mockito.Mockito;
import org.opensearch.cluster.ClusterName;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.opensearch.graph.executor.ontology.schema.IndexProviderRawSchema.getIndexPartitions;
import static org.opensearch.graph.test.framework.index.SearchEmbeddedNode.GRAPH_TEST_OPENSEARCH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by Roman on 21/06/2017.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        GraphInitiatorIT.class,
        IndexProviderBasedGraphLoaderIT.class,
        IndexProviderBasedCSVLoaderIT.class,
        OpensearchIndexProviderMappingFactoryIT.class
})
@Ignore("Migrate to OS")
public class TestSuiteIndexProviderSuite implements BaseSuiteMarker {
    private static SearchEmbeddedNode searchEmbeddedNode;

    public static ObjectMapper mapper = new ObjectMapper();
    public static Config config;
    public static Ontology ontology;

    public static RawSchema nestedSchema, embeddedSchema, singleIndexSchema;
    public static IndexProvider nestedProvider, embeddedProvider, singleIndexProvider;

    public static OntologyProvider ontologyProvider;
    public static IndexProviderFactory nestedProviderIfc, embeddedProviderIfc, singleIndexProviderFactory;

    public static Client client;

    public static void setUpInternal() throws Exception {
        client = SearchEmbeddedNode.getClient();
        InputStream providerNestedStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/DragonsIndexProviderNested.conf");
        InputStream providerEmbeddedStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/DragonsIndexProviderEmbedded.conf");
        InputStream providerSingleIndexStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/DragonsSingleIndexProvider.conf");
        InputStream ontologyStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/Dragons.json");

        nestedProvider = mapper.readValue(providerNestedStream, IndexProvider.class);
        embeddedProvider = mapper.readValue(providerEmbeddedStream, IndexProvider.class);
        singleIndexProvider = mapper.readValue(providerSingleIndexStream, IndexProvider.class);
        ontology = mapper.readValue(ontologyStream, Ontology.class);


        nestedProviderIfc = Mockito.mock(IndexProviderFactory.class);
        when(nestedProviderIfc.get(any())).thenAnswer(invocationOnMock -> Optional.of(nestedProvider));

        embeddedProviderIfc = Mockito.mock(IndexProviderFactory.class);
        when(embeddedProviderIfc.get(any())).thenAnswer(invocationOnMock -> Optional.of(embeddedProvider));

        singleIndexProviderFactory = Mockito.mock(IndexProviderFactory.class);
        when(singleIndexProviderFactory.get(any())).thenAnswer(invocationOnMock -> Optional.of(singleIndexProvider));

        ontologyProvider = Mockito.mock(OntologyProvider.class);
        when(ontologyProvider.get(any())).thenAnswer(invocationOnMock -> Optional.of(ontology));

        config = Mockito.mock(Config.class);
        when(config.getString(any())).thenAnswer(invocationOnMock -> "Dragons");

        GraphElementSchemaProvider nestedSchemaProvider = new GraphElementSchemaProviderJsonFactory(config, nestedProviderIfc, ontologyProvider).get(ontology);
        GraphElementSchemaProvider embeddedSchemaProvider = new GraphElementSchemaProviderJsonFactory(config, embeddedProviderIfc, ontologyProvider).get(ontology);
        GraphElementSchemaProvider singleIndexSchemaProvider = new GraphElementSchemaProviderJsonFactory(config, singleIndexProviderFactory, ontologyProvider).get(ontology);

        nestedSchema = new RawSchema() {
            @Override
            public IndexPartitions getPartition(String type) {
                return getIndexPartitions(nestedSchemaProvider, type);
            }

            @Override
            public String getIdPrefix(String type) {
                return "";
            }

            @Override
            public String getIdFormat(String type) {
                return "";
            }

            @Override
            public String getIndexPrefix(String type) {
                return "";
            }

            @Override
            public List<IndexPartitions.Partition> getPartitions(String type) {
                return StreamSupport.stream(getPartition(type).getPartitions().spliterator(), false)
                        .collect(Collectors.toList());

            }

            @Override
            public Iterable<String> indices() {
                return IndexProviderRawSchema.indices(nestedSchemaProvider);
            }
        };

        embeddedSchema = new RawSchema() {
            @Override
            public IndexPartitions getPartition(String type) {
                return getIndexPartitions(embeddedSchemaProvider, type);
            }

            @Override
            public String getIdPrefix(String type) {
                return "";
            }

            @Override
            public String getIdFormat(String type) {
                return "";
            }

            @Override
            public String getIndexPrefix(String type) {
                return "";
            }

            @Override
            public List<IndexPartitions.Partition> getPartitions(String type) {
                return StreamSupport.stream(getPartition(type).getPartitions().spliterator(), false)
                        .collect(Collectors.toList());

            }

            @Override
            public Iterable<String> indices() {
                return IndexProviderRawSchema.indices(embeddedSchemaProvider);
            }
        };

        singleIndexSchema = new RawSchema() {
            @Override
            public IndexPartitions getPartition(String type) {
                return getIndexPartitions(singleIndexSchemaProvider, type);
            }

            @Override
            public String getIdPrefix(String type) {
                return "";
            }

            @Override
            public String getIdFormat(String type) {
                return "";
            }

            @Override
            public String getIndexPrefix(String type) {
                return "";
            }

            @Override
            public List<IndexPartitions.Partition> getPartitions(String type) {
                return StreamSupport.stream(getPartition(type).getPartitions().spliterator(), false)
                        .collect(Collectors.toList());

            }

            @Override
            public Iterable<String> indices() {
                return IndexProviderRawSchema.indices(singleIndexSchemaProvider);
            }
        };
    }

    @BeforeClass
    public static void setup() throws Exception {
        init(true);
        //init elasticsearch provider mapping factory
        setUpInternal();
    }

    private static void init(boolean embedded) throws Exception {
        //first verify no instance is running already
        Optional<org.opensearch.client.core.MainResponse> info = GlobalSearchEmbeddedNode.isRunningLocally();
        // Start embedded ES
        if (embedded && !info.isPresent()) {
            info = Optional.of(getDefaultInfo());
            searchEmbeddedNode = GlobalSearchEmbeddedNode.getInstance(info.get().getNodeName());
        }
        //use existing running ES
        client = SearchEmbeddedNode.getClient(info.orElseGet(TestSuiteIndexProviderSuite::getDefaultInfo));
    }

    private static org.opensearch.client.core.MainResponse getDefaultInfo() {
        return new org.opensearch.client.core.MainResponse(GRAPH_TEST_OPENSEARCH, null, ClusterName.DEFAULT.toString(),null);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        GlobalSearchEmbeddedNode.close();
    }


    public static Client getClient() {
        return client;
    }

    //region Fields
    private static Jooby app;
    //endregion
}
