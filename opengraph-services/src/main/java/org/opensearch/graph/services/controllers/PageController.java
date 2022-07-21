package org.opensearch.graph.services.controllers;


import org.opensearch.graph.model.resourceInfo.PageResourceInfo;
import org.opensearch.graph.model.resourceInfo.StoreResourceInfo;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.model.transport.CreatePageRequest;
import org.opensearch.graph.model.transport.cursor.LogicalGraphCursorRequest;

/**
 * Created by lior.perry on 19/02/2017.
 */
public interface PageController<C,D> extends Controller<C,D>{
    /**
     * get the page resource information
     * @param queryId
     * @param cursorId
     * @param createPageRequest
     * @return
     */
    ContentResponse<PageResourceInfo> create(String queryId, String cursorId, CreatePageRequest createPageRequest);
    /**
     * get the page resource information
     * @param queryId
     * @param cursorId
     * @param createPageRequest
     * @return
     */
    ContentResponse<PageResourceInfo> createAndFetch(String queryId, String cursorId, CreatePageRequest createPageRequest);
    /**
     * get the page resource information
     * @param queryId
     * @param cursorId
     * @return
     */
    ContentResponse<StoreResourceInfo> getInfo(String queryId, String cursorId);
    /**
     * get the page resource information
     * @param queryId
     * @param cursorId
     * @param pageId
     * @return
     */
    ContentResponse<PageResourceInfo> getInfo(String queryId, String cursorId, String pageId);
    /**
     * delete the page resource including the cached data
     * @param queryId
     * @param cursorId
     * @param pageId
     * @return
     */
    ContentResponse<Boolean> delete(String queryId, String cursorId, String pageId);
    /**
     * get the data from the cached page resource
     * @param queryId
     * @param cursorId
     * @param pageId
     * @return
     */
    ContentResponse<Object> getData(String queryId, String cursorId, String pageId);

    /**
     * format the data according to the specific graph format
     * @param queryId
     * @param cursorId
     * @param pageId
     * @param format
     * @return
     */
    ContentResponse<Object> format(String queryId, String cursorId, String pageId, LogicalGraphCursorRequest.GraphFormat format);
}
