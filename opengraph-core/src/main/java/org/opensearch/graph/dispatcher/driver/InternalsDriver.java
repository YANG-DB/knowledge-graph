package org.opensearch.graph.dispatcher.driver;




import java.util.Optional;

/**
 * Created by lior.perry on 21/02/2017.
 */
public interface InternalsDriver {
    Optional<com.typesafe.config.Config> getConfig();
    Optional<String> getStatisticsProviderName();
    Optional<String> getStatisticsProviderSetup();
    Optional<String> refreshStatisticsProviderSetup();
}
