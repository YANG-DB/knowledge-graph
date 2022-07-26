package org.opensearch.graph.epb.plan.statistics;





public interface RefreshableStatisticsProviderFactory extends StatisticsProviderFactory{
    void refresh();
    String getSetup();
}
