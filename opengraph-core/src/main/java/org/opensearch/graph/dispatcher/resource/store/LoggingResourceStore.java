package org.opensearch.graph.dispatcher.resource.store;







import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.opensearch.graph.dispatcher.resource.CursorResource;
import org.opensearch.graph.dispatcher.resource.PageResource;
import org.opensearch.graph.dispatcher.resource.QueryResource;
import org.opensearch.graph.model.descriptors.Descriptor;
import org.opensearch.graph.model.transport.CreateQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

public class LoggingResourceStore implements ResourceStore {
    public static final String injectionName = "LoggingResourceStore.inner";

    //region Constructors
    @Inject
    public LoggingResourceStore(
            @Named(injectionName) ResourceStore innerResourceStore,
            Descriptor<QueryResource> queryResourceDescriptor,
            Descriptor<CursorResource> cursorResourceDescriptor,
            Descriptor<PageResource> pageResourceDescriptor) {

        this.logger = LoggerFactory.getLogger(innerResourceStore.getClass());
        this.innerResourceStore = innerResourceStore;
        this.queryResourceDescriptor = queryResourceDescriptor;
        this.cursorResourceDescriptor = cursorResourceDescriptor;
        this.pageResourceDescriptor = pageResourceDescriptor;
    }
    //endregion

    //region ResourceStore Implementation
    @Override
    public Collection<QueryResource> getQueryResources() {
        return this.innerResourceStore.getQueryResources();
    }

    @Override
    public Collection<QueryResource> getQueryResources(Predicate<String> predicate) {
        return this.innerResourceStore.getQueryResources(predicate);
    }

    @Override
    public Optional<QueryResource> getQueryResource(String queryId) {
        return this.innerResourceStore.getQueryResource(queryId);
    }

    @Override
    public Optional<CursorResource> getCursorResource(String queryId, String cursorId) {
        return this.innerResourceStore.getCursorResource(queryId, cursorId);
    }

    @Override
    public Optional<PageResource> getPageResource(String queryId, String cursorId, String pageId) {
        return this.innerResourceStore.getPageResource(queryId, cursorId, pageId);
    }

    @Override
    public boolean addQueryResource(QueryResource queryResource) {
        boolean b = this.innerResourceStore.addQueryResource(queryResource);
        this.logger.info("QueryResource was added: {}", this.queryResourceDescriptor.describe(queryResource));
        return b;
    }

    @Override
    public boolean deleteQueryResource(String queryId) {
        Optional<QueryResource> queryResourceToDelete = this.innerResourceStore.getQueryResource(queryId);
        boolean b = this.innerResourceStore.deleteQueryResource(queryId);

        if (queryResourceToDelete.isPresent()) {
            this.logger.debug("QueryResource was deleted: {}", this.queryResourceDescriptor.describe(queryResourceToDelete.get()));
        }
        return b;
    }

    @Override
    public boolean addCursorResource(String queryId, CursorResource cursorResource) {
        Optional<QueryResource> queryResource = this.innerResourceStore.getQueryResource(queryId);
        boolean b = this.innerResourceStore.addCursorResource(queryId, cursorResource);

        if (queryResource.isPresent()) {
            this.logger.debug("CursorResource was added: {} {}",
                    this.queryResourceDescriptor.describe(queryResource.get()),
                    this.cursorResourceDescriptor.describe(cursorResource));
        }
        return b;
    }

    @Override
    public boolean deleteCursorResource(String queryId, String cursorId) {
        Optional<QueryResource> queryResource = this.innerResourceStore.getQueryResource(queryId);
        Optional<CursorResource> cursorResourceToDelete = queryResource.flatMap(queryResource1 -> queryResource1.getCursorResource(cursorId));
        boolean b = this.innerResourceStore.deleteCursorResource(queryId, cursorId);

        if (queryResource.isPresent() && cursorResourceToDelete.isPresent()) {
            this.logger.debug("CursorResource was deleted: {} {}",
                    this.queryResourceDescriptor.describe(queryResource.get()),
                    this.cursorResourceDescriptor.describe(cursorResourceToDelete.get()));
        }
        return b;
    }

    @Override
    public boolean addPageResource(String queryId, String cursorId, PageResource pageResource) {
        Optional<QueryResource> queryResource = this.innerResourceStore.getQueryResource(queryId);
        Optional<CursorResource> cursorResource = queryResource.flatMap(queryResource1 -> queryResource1.getCursorResource(cursorId));

        boolean b = this.innerResourceStore.addPageResource(queryId, cursorId, pageResource);

        if (queryResource.isPresent() && cursorResource.isPresent()) {
            if (pageResource.isAvailable()) {
                this.logger.debug("PageResource is available: {} {} {}",
                        this.queryResourceDescriptor.describe(queryResource.get()),
                        this.cursorResourceDescriptor.describe(cursorResource.get()),
                        this.pageResourceDescriptor.describe(pageResource));
            } else {
                this.logger.debug("PageResource was added: {} {} {}",
                        this.queryResourceDescriptor.describe(queryResource.get()),
                        this.cursorResourceDescriptor.describe(cursorResource.get()),
                        this.pageResourceDescriptor.describe(pageResource));
            }
        }
        return b;
    }

    @Override
    public boolean deletePageResource(String queryId, String cursorId, String pageId) {
        Optional<QueryResource> queryResource = this.innerResourceStore.getQueryResource(queryId);
        Optional<CursorResource> cursorResource = queryResource.flatMap(queryResource1 -> queryResource1.getCursorResource(cursorId));
        Optional<PageResource> pageResourceToDelete = cursorResource.flatMap(cursorResource1 -> cursorResource1.getPageResource(pageId));

        boolean b = this.innerResourceStore.deletePageResource(queryId, cursorId, pageId);

        if (queryResource.isPresent() && cursorResource.isPresent() && pageResourceToDelete.isPresent()) {
            this.logger.debug("PageResource was deleted: {} {} {}",
                    this.queryResourceDescriptor.describe(queryResource.get()),
                    this.cursorResourceDescriptor.describe(cursorResource.get()),
                    this.pageResourceDescriptor.describe(pageResourceToDelete.get()));
        }
        return b;
    }
    //endregion

    //region Fields
    private Logger logger;

    private ResourceStore innerResourceStore;

    private Descriptor<QueryResource> queryResourceDescriptor;
    private Descriptor<CursorResource> cursorResourceDescriptor;
    private Descriptor<PageResource> pageResourceDescriptor;

    @Override
    public boolean test(CreateQueryRequest.StorageType type) {
        return this.innerResourceStore.test(type);
    }
    //endregion
}
