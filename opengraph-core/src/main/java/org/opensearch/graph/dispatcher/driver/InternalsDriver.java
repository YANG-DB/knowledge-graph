package org.opensearch.graph.dispatcher.driver;







import java.util.Optional;

public interface InternalsDriver {
    Optional<com.typesafe.config.Config> getConfig();
    Optional<String> getStatisticsProviderName();
    Optional<String> getStatisticsProviderSetup();
    Optional<String> refreshStatisticsProviderSetup();
}
