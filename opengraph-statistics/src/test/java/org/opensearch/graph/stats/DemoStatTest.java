package org.opensearch.graph.stats;

import org.opensearch.graph.stats.configuration.StatConfiguration;
import org.junit.Ignore;
import org.junit.Test;

import static org.opensearch.graph.stats.StatTestSuite.dataClient;
import static org.opensearch.graph.stats.StatTestSuite.statClient;

/**
 * Created by benishue on 07-Jun-17.
 */
public class DemoStatTest {

    private static final String CONFIGURATION_FILE_PATH = "statistics.demo.properties";

    @Ignore
    @Test
    public void runDemo() throws Exception {
        StatCalculator.run(dataClient, statClient, new StatConfiguration(CONFIGURATION_FILE_PATH).getInstance());
    }
}
