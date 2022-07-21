package org.opensearch.graph.dispatcher.resource.store;




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
