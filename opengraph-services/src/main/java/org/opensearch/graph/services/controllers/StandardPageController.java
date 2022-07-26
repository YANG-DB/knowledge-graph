package org.opensearch.graph.services.controllers;




import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.driver.PageDriver;
import org.opensearch.graph.model.resourceInfo.PageResourceInfo;
import org.opensearch.graph.model.resourceInfo.StoreResourceInfo;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.model.transport.ContentResponse.Builder;
import org.opensearch.graph.model.transport.CreatePageRequest;
import org.opensearch.graph.model.transport.cursor.LogicalGraphCursorRequest;

import java.util.Optional;

import static org.opensearch.graph.model.transport.ContentResponse.Builder.builder;
import static org.opensearch.graph.model.transport.Status.*;

/**
 * Created by lior.perry on 19/02/2017.
 */
public class StandardPageController implements PageController<PageController,PageDriver> {
    //region Constructors
    @Inject
    public StandardPageController(PageDriver driver) {
        this.driver = driver;
    }
    //endregion

    //region PageController Implementation
    @Override
    public ContentResponse<PageResourceInfo> create(String queryId, String cursorId, CreatePageRequest createPageRequest) {
        return Builder.<PageResourceInfo>builder(CREATED, INTERNAL_SERVER_ERROR)
                .data(driver().create(queryId, cursorId, createPageRequest.getPageSize()))
                .compose();
    }

    @Override
    public ContentResponse<PageResourceInfo> createAndFetch(String queryId, String cursorId, CreatePageRequest createPageRequest) {
        ContentResponse<PageResourceInfo> pageResourceInfoResponse = this.create(queryId, cursorId, createPageRequest);
        if (pageResourceInfoResponse.status() == INTERNAL_SERVER_ERROR) {
            return Builder.<PageResourceInfo>builder(CREATED, INTERNAL_SERVER_ERROR)
                    .data(Optional.of(pageResourceInfoResponse.getData()))
                    .successPredicate(response -> false)
                    .compose();
        }

        ContentResponse<Object> pageDataResponse = this.getData(queryId, cursorId, pageResourceInfoResponse.getData().getResourceId());
        if (pageDataResponse.status() == INTERNAL_SERVER_ERROR) {
            return Builder.<PageResourceInfo>builder(CREATED, INTERNAL_SERVER_ERROR)
                    .data(Optional.of(pageResourceInfoResponse.getData()))
                    .successPredicate(response -> false)
                    .compose();
        }

        pageResourceInfoResponse.getData().setData(pageDataResponse.getData());

        return Builder.<PageResourceInfo>builder(CREATED, INTERNAL_SERVER_ERROR)
                .data(Optional.of(pageResourceInfoResponse.getData()))
                .compose();
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo(String queryid, String cursorId) {
        return Builder.<StoreResourceInfo>builder(OK, NOT_FOUND)
                .data(driver().getInfo(queryid, cursorId))
                .compose();
    }

    @Override
    public ContentResponse<PageResourceInfo> getInfo(String queryId, String cursorId, String pageId) {
        return Builder.<PageResourceInfo>builder(OK, NOT_FOUND)
                .data(driver().getInfo(queryId, cursorId, pageId))
                .compose();
    }

    @Override
    public ContentResponse<Object> getData(String queryId, String cursorId, String pageId) {
        return builder(OK, NOT_FOUND)
                .data(driver().getData(queryId, cursorId, pageId))
                .compose();
    }

    @Override
    public ContentResponse<Object> format(String queryId, String cursorId, String pageId, LogicalGraphCursorRequest.GraphFormat format) {
        return builder(OK, NOT_FOUND)
                .data(driver().format(queryId, cursorId, pageId,format))
                .compose();
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId, String cursorId, String pageId) {
        return ContentResponse.Builder.<Boolean>builder(ACCEPTED, NOT_FOUND)
                .data(driver().delete(queryId, cursorId, pageId))
                .compose();
    }

    protected PageDriver driver() {
        return driver;
    }
    //endregion

    //region Fields
    private PageDriver driver;

    @Override
    public StandardPageController driver(PageDriver driver) {
        this.driver = driver;
        return this;
    }
    //endregion
}
