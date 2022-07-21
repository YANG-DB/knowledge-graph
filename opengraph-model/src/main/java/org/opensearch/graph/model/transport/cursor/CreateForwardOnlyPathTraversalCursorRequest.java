package org.opensearch.graph.model.transport.cursor;

/*-
 * #%L
 * opengraph-model
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


import org.opensearch.graph.model.transport.CreatePageRequest;

public class CreateForwardOnlyPathTraversalCursorRequest extends CreateCursorRequest {

    public static final String CursorType = "forwardPaths";
    //region Constructors
    public CreateForwardOnlyPathTraversalCursorRequest() {
        super(CursorType);
    }

    public CreateForwardOnlyPathTraversalCursorRequest(CreatePageRequest createPageRequest) {
        super(CursorType, createPageRequest);
    }

    public CreateForwardOnlyPathTraversalCursorRequest(Include include, CreatePageRequest createPageRequest) {
        super(CursorType, include, createPageRequest);
    }
}
