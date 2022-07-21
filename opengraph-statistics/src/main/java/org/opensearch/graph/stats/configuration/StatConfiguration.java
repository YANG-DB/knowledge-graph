package org.opensearch.graph.stats.configuration;


import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

public class StatConfiguration {

    private Configuration configuration;

    public StatConfiguration(String configPath) throws Exception {
        configuration = setConfiguration(configPath);
    }

    private synchronized Configuration setConfiguration(String configPath) throws Exception {
        if (configuration != null) {
            return configuration;
        }
        configuration = new PropertiesConfiguration(configPath);

        return configuration;
    }

    public Configuration getInstance() {
        return configuration;
    }
}
