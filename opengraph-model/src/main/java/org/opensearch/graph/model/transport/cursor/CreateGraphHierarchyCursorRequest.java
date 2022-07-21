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

import java.util.Collections;

/**
 * Created by roman.margolis on 11/03/2018.
 */
public class CreateGraphHierarchyCursorRequest extends CreateGraphCursorRequest {
    public static final String CursorType = "graphHierarchy";

    //region Constructors
    public CreateGraphHierarchyCursorRequest() {
        this(Collections.emptyList());
    }

    public CreateGraphHierarchyCursorRequest(Iterable<String> countTags) {
        this(countTags, null);
    }

    public CreateGraphHierarchyCursorRequest(Iterable<String> countTags, CreatePageRequest createPageRequest) {
        this(Include.all, countTags, createPageRequest);
    }

    public CreateGraphHierarchyCursorRequest(Iterable<String> countTags, CreatePageRequest createPageRequest,GraphFormat format) {
        this(Include.all, countTags, createPageRequest,format);
    }

    public CreateGraphHierarchyCursorRequest(Include include, Iterable<String> countTags, CreatePageRequest createPageRequest) {
        super(CursorType, include, createPageRequest);
        this.countTags = countTags;
    }

    public CreateGraphHierarchyCursorRequest(Include include, Iterable<String> countTags, CreatePageRequest createPageRequest,GraphFormat format) {
        super(CursorType, include, createPageRequest,format);
        this.countTags = countTags;
    }
    //endregion

    //region Properties
    public Iterable<String> getCountTags() {
        return countTags;
    }

    public void setCountTags(Iterable<String> countTags) {
        this.countTags = countTags;
    }
    //endregion

    //region Fields
    private Iterable<String> countTags;
    //endregion
}
