package org.opensearch.graph.epb.utils;

import org.opensearch.graph.epb.plan.statistics.GraphStatisticsProvider;
import org.mockito.Mockito;

/**
 * Created by moti on 5/16/2017.
 */
public interface GraphStatisticsProviderMock  {
    static GraphStatisticsProvider mock(PlanMockUtils.PlanMockBuilder planBuilder) {
        GraphStatisticsProvider mock = Mockito.mock(GraphStatisticsProvider.class);

        //mock statistics provider
        return mock;
    }
}
