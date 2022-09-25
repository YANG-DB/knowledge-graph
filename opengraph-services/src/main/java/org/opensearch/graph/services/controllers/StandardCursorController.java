package org.opensearch.graph.services.controllers;

/*-
 * #%L
 * opengraph-services
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */




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
