package org.opensearch.graph.dispatcher.driver;

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






import com.google.inject.Inject;
import org.opensearch.graph.client.export.GraphWriterStrategy;
import org.opensearch.graph.dispatcher.resource.CursorResource;
import org.opensearch.graph.dispatcher.resource.PageResource;
import org.opensearch.graph.dispatcher.resource.QueryResource;
import org.opensearch.graph.dispatcher.resource.store.ResourceStore;
import org.opensearch.graph.dispatcher.urlSupplier.AppUrlSupplier;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.resourceInfo.GraphError;
import org.opensearch.graph.model.resourceInfo.PageResourceInfo;
import org.opensearch.graph.model.resourceInfo.QueryResourceInfo;
import org.opensearch.graph.model.resourceInfo.StoreResourceInfo;
import org.opensearch.graph.model.results.Assignment;
import org.opensearch.graph.model.results.AssignmentsQueryResult;
import org.opensearch.graph.model.results.QueryResultBase;
import org.opensearch.graph.model.transport.cursor.CreateGraphCursorRequest;
import org.opensearch.graph.model.transport.cursor.LogicalGraphCursorRequest;
import javaslang.collection.Stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

public abstract class PageDriverBase implements PageDriver {

    //todo replace with proper writers injected via IOC
    private GraphWriterStrategy writerStrategy;


    //region Constructors
    @Inject
    public PageDriverBase(ResourceStore resourceStore, AppUrlSupplier urlSupplier, GraphWriterStrategy writerStrategy) {
        this.resourceStore = resourceStore;
        this.urlSupplier = urlSupplier;
        this.writerStrategy = writerStrategy;

    }
    //endregion

    //region PageDriver Implementation
    @Override
    public Optional<PageResourceInfo> create(String queryId, String cursorId, int pageSize) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        Optional<CursorResource> cursorResource = queryResource.get().getCursorResource(cursorId);
        if (!cursorResource.isPresent()) {
            return Optional.empty();
        }

        //outer page id resource
        String pageId = cursorResource.get().getNextPageId();
        //create inner page resources
        createInnerPage(queryResource.get(), cursorId, pageSize);
        PageResource<QueryResultBase> pageResource = this.createResource(queryResource.get(), cursorResource.get(), pageId, pageSize);
        this.resourceStore.addPageResource(queryId, cursorId, pageResource);

        return Optional.of(new PageResourceInfo(
                urlSupplier.resourceUrl(queryId, cursorId, pageId),
                pageId,
                pageSize,
                pageResource.getActualSize(),
                0,
                true));
    }

    private void createInnerPage(QueryResource queryResource, String cursorId, int pageSize) {
        queryResource.getInnerQueryResources().forEach(inner -> {
            create(inner.getQueryMetadata().getId(), cursorId, pageSize);
        });
    }

    @Override
    public Optional<StoreResourceInfo> getInfo(String queryId, String cursorId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        Optional<CursorResource> cursorResource = queryResource.get().getCursorResource(cursorId);
        if (!cursorResource.isPresent()) {
            return Optional.empty();
        }

        Iterable<String> resourceUrls = Stream.ofAll(cursorResource.get().getPageResources())
                .sortBy(pageResource -> pageResource.getTimeCreated())
                .map(pageResource -> pageResource.getPageId())
                .map(pageId -> this.urlSupplier.resourceUrl(queryId, cursorId, pageId))
                .toJavaList();

        return Optional.of(new StoreResourceInfo(this.urlSupplier.pageStoreUrl(queryId, cursorId), null, resourceUrls));
    }

    @Override
    public Optional<PageResourceInfo> getInfo(String queryId, String cursorId, String pageId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        Optional<CursorResource> cursorResource = queryResource.get().getCursorResource(cursorId);
        if (!cursorResource.isPresent()) {
            return Optional.empty();
        }

        Optional<PageResource> pageResource = cursorResource.get().getPageResource(pageId);
        if (!pageResource.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(new PageResourceInfo(this.urlSupplier.resourceUrl(queryId, cursorId, pageId),
                pageId,
                pageResource.get().getRequestedSize(),
                pageResource.get().getActualSize(),
                pageResource.get().getExecutionTime(),
                pageResource.get().isAvailable(),
                pageResource.get().getData()));
    }

    @Override
    public Optional<Object> getData(String queryId, String cursorId, String pageId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        Optional<CursorResource> cursorResource = queryResource.get().getCursorResource(cursorId);
        if (!cursorResource.isPresent()) {
            return Optional.empty();
        }

        Optional<PageResource> pageResource = cursorResource.get().getPageResource(pageId);
        if (!pageResource.isPresent()) {
            return Optional.empty();
        }

        return Optional.ofNullable(pageResource.get().getData());
    }

    @Override
    public Optional<Object> format(String queryId, String cursorId, String pageId, LogicalGraphCursorRequest.GraphFormat format) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        Optional<CursorResource> cursorResource = queryResource.get().getCursorResource(cursorId);
        if (!cursorResource.isPresent()) {
            return Optional.empty();
        }

        if (cursorResource.get().getCursorRequest() instanceof CreateGraphCursorRequest) {
            try {
                AssignmentsQueryResult result = (AssignmentsQueryResult) getData(queryId, cursorId, pageId).get();
                Assignment graph = (Assignment) result.getAssignments().get(0);
                if (writerStrategy.writer(format).isPresent()) {
                    final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    writerStrategy.writer(format).get().writeGraph(stream, graph);
                    return Optional.of(new String(stream.toByteArray()));
                }
                return Optional.of(result);
            } catch (IOException e) {
                return Optional.of(new QueryResourceInfo().error(
                        new GraphError(Query.class.getSimpleName(), e)));
            }
        }
        return Optional.empty();

    }

    @Override
    public Optional<Boolean> delete(String queryId, String cursorId, String pageId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        //delete inner query pages
        queryResource.get().getInnerQueryResources().forEach(inner -> delete(inner.getQueryMetadata().getId(), cursorId, pageId));
        //delete outer resources
        Optional<CursorResource> cursorResource = queryResource.get().getCursorResource(cursorId);
        if (!cursorResource.isPresent()) {
            return Optional.empty();
        }

        cursorResource.get().deletePageResource(pageId);
        return Optional.of(true);
    }
    //endregion

    //region Protected Abstract Methods
    protected abstract PageResource<QueryResultBase> createResource(QueryResource queryResource, CursorResource cursorResource, String pageId, int pageSize);
    //endregion

    //region Fields
    protected ResourceStore resourceStore;
    protected final AppUrlSupplier urlSupplier;
    //endregion
}
