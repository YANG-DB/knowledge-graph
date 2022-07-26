package org.opensearch.graph.dispatcher.driver;







import org.opensearch.graph.model.resourceInfo.PageResourceInfo;
import org.opensearch.graph.model.resourceInfo.StoreResourceInfo;
import org.opensearch.graph.model.transport.cursor.LogicalGraphCursorRequest;

import java.util.Optional;

public interface PageDriver {
    /**
     * create a new page resource that will activate the next scroll over the data
     * @param queryId
     * @param cursorId
     * @param pageSize
     * @return
     */
    Optional<PageResourceInfo> create(String queryId, String cursorId, int pageSize);

    /**
     * get the page resource information
     * @param queryId
     * @param cursorId
     * @return
     */
    Optional<StoreResourceInfo> getInfo(String queryId, String cursorId);

    /**
     * get the page resource information
     * @param queryId
     * @param cursorId
     * @param pageId
     * @return
     */
    Optional<PageResourceInfo> getInfo(String queryId, String cursorId, String pageId);

    /**
     * delete the page resource including the cached data
     * @param queryId
     * @param cursorId
     * @param pageId
     * @return
     */
    Optional<Boolean> delete(String queryId, String cursorId, String pageId);

    /**
     * get the data from the cached page resource
     * @param queryId
     * @param cursorId
     * @param pageId
     * @return
     */
    Optional<Object> getData(String queryId, String cursorId, String pageId);

    /**
     * format the data according to the specific graph format
     * @param queryId
     * @param cursorId
     * @param pageId
     * @param format
     * @return
     */
    Optional<Object> format(String queryId, String cursorId, String pageId, LogicalGraphCursorRequest.GraphFormat format);
}
