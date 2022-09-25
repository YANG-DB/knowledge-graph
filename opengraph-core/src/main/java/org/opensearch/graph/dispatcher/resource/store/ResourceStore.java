package org.opensearch.graph.dispatcher.resource.store;

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







import org.opensearch.graph.dispatcher.resource.CursorResource;
import org.opensearch.graph.dispatcher.resource.PageResource;
import org.opensearch.graph.dispatcher.resource.QueryResource;
import org.opensearch.graph.model.transport.CreateQueryRequest;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

public interface ResourceStore extends Predicate<CreateQueryRequest.StorageType> {
    Collection<QueryResource> getQueryResources();
    Collection<QueryResource> getQueryResources(Predicate<String> predicate);
    Optional<QueryResource> getQueryResource(String queryId);
    Optional<CursorResource> getCursorResource(String queryId, String cursorId);
    Optional<PageResource> getPageResource(String queryId, String cursorId, String pageId);

    boolean addQueryResource(QueryResource queryResource);
    boolean deleteQueryResource(String queryId);

    boolean addCursorResource(String queryId, CursorResource cursorResource);
    boolean deleteCursorResource(String queryId, String cursorId);

    boolean addPageResource(String queryId, String cursorId, PageResource pageResource);
    boolean deletePageResource(String queryId, String cursorId, String pageId);
}
