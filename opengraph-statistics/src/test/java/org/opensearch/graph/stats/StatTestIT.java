package org.opensearch.graph.stats;

import org.junit.Ignore;
import org.opensearch.client.transport.TransportClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.opensearch.graph.test.BaseSuiteMarker;
import org.opensearch.graph.test.framework.index.SearchEmbeddedNode;
import org.opensearch.graph.test.framework.index.GlobalSearchEmbeddedNode;

import java.nio.file.Paths;

/**
 * Created by Roman on 20/06/2017.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        DemoStatIT.class,
        SearchStatsUtilsIT.class,
        StatCalculatorDynamicFieldIT.class,
        StatCalculatorIT.class,
        StatConfigurationIT.class
})
@Ignore("Must be upgrades to support opensearch new version")
public class StatTestIT implements BaseSuiteMarker {
    private static final String CONFIGURATION_FILE_PATH = "statistics.test.properties";

    public static final String MAPPING_DATA_FILE_DRAGON_PATH = Paths.get("src", "test", "resources", "elastic.test.data.dragon.mapping.json").toString();
    public static final String MAPPING_DATA_FILE_FIRE_PATH = Paths.get("src", "test", "resources", "elastic.test.data.fire.mapping.json").toString();
    public static final String MAPPING_STAT_FILE_PATH = Paths.get("src", "test", "resources", "elastic.test.stat.mapping.json").toString();
    public static final String STAT_INDEX_NAME = "stat";

    public static TransportClient dataClient;
    public static TransportClient statClient;

    private static SearchEmbeddedNode searchEmbeddedNode;

    @BeforeClass
    public static void setup() throws Exception {
        searchEmbeddedNode = GlobalSearchEmbeddedNode.getInstance();

        dataClient = SearchEmbeddedNode.getClient();
        statClient = SearchEmbeddedNode.getClient();
    }

    @AfterClass
    public static void tearDown() throws Exception {
    }
}
