package org.opensearch.graph.services.controllers;


import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.model.transport.cursor.CreateCursorRequest;

import java.util.Map;

/**
 * Created by lior.perry on 19/02/2017.
 */
public interface InternalsController<C,D> extends Controller<C,D>{

    ContentResponse<String> getVersion();
    ContentResponse<Long> getSnowflakeId();
    ContentResponse<Map<String, Class<? extends CreateCursorRequest>>> getCursorBindings();
    ContentResponse<String> getStatisticsProviderName();
    ContentResponse<Map> getConfig();
    ContentResponse<String> getStatisticsProviderSetup();
    ContentResponse<String> refreshStatisticsProviderSetup();
}
