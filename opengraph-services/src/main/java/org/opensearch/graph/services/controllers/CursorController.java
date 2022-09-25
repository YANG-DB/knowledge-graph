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
