package org.opensearch.graph.services.controllers;




import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.driver.CursorDriver;
import org.opensearch.graph.model.resourceInfo.CursorResourceInfo;
import org.opensearch.graph.model.resourceInfo.StoreResourceInfo;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.model.transport.ContentResponse.Builder;
import org.opensearch.graph.model.transport.cursor.CreateCursorRequest;

import static org.opensearch.graph.model.transport.Status.*;

/**
 * Created by lior.perry on 19/02/2017.
 */
public class StandardCursorController implements CursorController<CursorController,CursorDriver> {
    //region Constructors
    @Inject
    public StandardCursorController(CursorDriver driver) {
        this.driver = driver;
    }
    //endregion

    //region CursorController Implementation
    @Override
    public ContentResponse<CursorResourceInfo> create(String queryId, CreateCursorRequest createCursorRequest) {
        return Builder.<CursorResourceInfo>builder(CREATED, INTERNAL_SERVER_ERROR)
                .data(driver().create(queryId, createCursorRequest))
                .compose();
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo(String queryId) {
        return Builder.<StoreResourceInfo>builder(OK, NOT_FOUND)
                .data(driver().getInfo(queryId))
                .compose();
    }

    @Override
    public ContentResponse<CursorResourceInfo> getInfo(String queryId, String cursorId) {
        return Builder.<CursorResourceInfo>builder(OK, NOT_FOUND)
                .data(driver().getInfo(queryId, cursorId))
                .compose();
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId, String cursorId) {
        return Builder.<Boolean>builder(ACCEPTED, NOT_FOUND)
                .data(driver().delete(queryId, cursorId)).compose();
    }

    protected CursorDriver driver() {
        return driver;
    }
    //endregion

    @Override
    public StandardCursorController driver(CursorDriver driver) {
        this.driver = driver;
        return this;
    }

    //region Fields
    private CursorDriver driver;

    //endregion
}
