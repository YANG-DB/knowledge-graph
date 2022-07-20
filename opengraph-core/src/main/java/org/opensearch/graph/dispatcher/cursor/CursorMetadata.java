package org.opensearch.graph.dispatcher.cursor;

/*-
 * #%L
 * opengraph-core
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



import org.opensearch.graph.model.transport.cursor.CreateCursorRequest;

public class CursorMetadata<T> {
    private CreateCursorRequest.Include include;
    private String queryId;
    private String cursorType;
    private T cursorParams;
    private long ttl;
    private long creationTime;

    public CursorMetadata(String queryId, String cursorType, T cursorParams,CreateCursorRequest.Include include, long ttl, long creationTime) {
        this.queryId = queryId;
        this.cursorType = cursorType;
        this.cursorParams = cursorParams;
        this.include = include;
        this.ttl = ttl;
        this.creationTime = creationTime;
    }

    public CreateCursorRequest.Include getInclude() {
        return include;
    }

    public String getQueryId() {
        return queryId;
    }

    public String getCursorType() {
        return cursorType;
    }

    public T getCursorParams() {
        return cursorParams;
    }

    public long getTtl() {
        return ttl;
    }

    public long getCreationTime() {
        return creationTime;
    }
}
