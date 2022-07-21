package org.opensearch.graph.services.controllers;


import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.driver.InternalsDriver;
import org.opensearch.graph.epb.plan.statistics.RefreshableStatisticsProviderFactory;
import org.opensearch.graph.epb.plan.statistics.StatisticsProviderFactory;
import com.typesafe.config.Config;

import java.util.Optional;

/**
 * Created by lior.perry on 20/02/2017.
 */
public class StandardInternalsDriver implements InternalsDriver {
    private Config config;
    private StatisticsProviderFactory statisticsProviderFactory;

    //region Constructors
    @Inject
    public StandardInternalsDriver(com.typesafe.config.Config config,StatisticsProviderFactory factory) {
        this.config = config;
        this.statisticsProviderFactory = factory;
    }

    @Override
    public Optional<Config> getConfig() {
        return Optional.of(config);
    }

    //endregion
    @Override
    public Optional<String> getStatisticsProviderName() {
        return Optional.of(statisticsProviderFactory.getClass().getSimpleName());
    }

    @Override
    public Optional<String> getStatisticsProviderSetup() {
        if (statisticsProviderFactory instanceof RefreshableStatisticsProviderFactory)
            return Optional.of(((RefreshableStatisticsProviderFactory) statisticsProviderFactory).getSetup());
        return getStatisticsProviderName();
    }

    @Override
    public Optional<String> refreshStatisticsProviderSetup() {
        if (statisticsProviderFactory instanceof RefreshableStatisticsProviderFactory) {
            ((RefreshableStatisticsProviderFactory) statisticsProviderFactory).refresh();
            return getStatisticsProviderSetup();
        }
        return Optional.empty();
    }
    //endregion
}
