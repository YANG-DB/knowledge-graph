package org.opensearch.graph.stats;

import org.opensearch.graph.stats.configuration.StatConfiguration;
import org.junit.Ignore;
import org.junit.Test;
import org.opensearch.graph.test.BaseITMarker;

import static org.opensearch.graph.stats.StatTestIT.dataClient;
import static org.opensearch.graph.stats.StatTestIT.statClient;

/**
 * Created by benishue on 07-Jun-17.
 */
@Ignore
public class DemoStatIT implements BaseITMarker {

    private static final String CONFIGURATION_FILE_PATH = "statistics.demo.properties";

    @Ignore
    @Test
    public void runDemo() throws Exception {
        StatCalculator.run(dataClient, statClient, new StatConfiguration(CONFIGURATION_FILE_PATH).getInstance());
    }
}
