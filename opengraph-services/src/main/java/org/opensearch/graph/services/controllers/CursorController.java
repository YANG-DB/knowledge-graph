package org.opensearch.graph.services.controllers;


import org.opensearch.graph.model.resourceInfo.CursorResourceInfo;
import org.opensearch.graph.model.resourceInfo.StoreResourceInfo;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.model.transport.cursor.CreateCursorRequest;

/**
 * Created by lior.perry on 22/02/2017.
 */

public interface CursorController<C,D> extends Controller<C,D>{
    /**
     *
     * @param queryId
     * @param createCursorRequest
     * @return
     */
    ContentResponse<CursorResourceInfo> create(String queryId, CreateCursorRequest createCursorRequest);

    /**
     *
     * @param queryId
     * @return
     */
    ContentResponse<StoreResourceInfo> getInfo(String queryId);

    /**
     *
     * @param queryId
     * @param cursorId
     * @return
     */
    ContentResponse<CursorResourceInfo> getInfo(String queryId, String cursorId);

    /**
     *
     * @param queryId
     * @param cursorId
     * @return
     */
    ContentResponse<Boolean> delete(String queryId, String cursorId);


}
