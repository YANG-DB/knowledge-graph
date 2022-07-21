package org.opensearch.graph.client;




import com.fasterxml.jackson.core.type.TypeReference;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.resourceInfo.*;
import org.opensearch.graph.model.results.*;
import org.opensearch.graph.model.transport.CreatePageRequest;
import org.opensearch.graph.model.transport.CreateQueryRequest;
import org.opensearch.graph.model.transport.cursor.CreateCursorRequest;
import org.opensearch.graph.model.transport.cursor.CreateGraphCursorRequest;

import java.io.IOException;
import java.util.function.Predicate;

public interface FuseClientSupport {

    static long countGraphElements(QueryResultBase pageData) {
        return countGraphElements(pageData, true, true, relationship -> true, entity -> true);
    }

    static long countGraphElements(QueryResultBase pageData, boolean relationship, boolean entities,
                                   Predicate<Relationship> relPredicate, Predicate<Entity> entityPredicate) {
        if (pageData instanceof CsvQueryResult)
            throw new IllegalArgumentException("Cursor returned CsvQueryResult instead of AssignmentsQueryResult");

        if (pageData.getSize() == 0)
            return 0;

        if (pageData instanceof AssignmentsQueryResult
                && ((AssignmentsQueryResult) pageData).getAssignments().isEmpty())
            return 0;

        return ((AssignmentsQueryResult<Entity,Relationship>) pageData).getAssignments().stream()
                .mapToLong(e -> (relationship ? e.getRelationships().stream().filter(relPredicate).count() : 0)
                        + (entities ? e.getEntities().stream().filter(entityPredicate).count() : 0))
                .sum();
    }

    static QueryResourceInfo query(GraphClient graphClient, FuseResourceInfo fuseResourceInfo, CreateQueryRequest request) throws IOException {
        return graphClient.postQuery(fuseResourceInfo.getQueryStoreUrl(),request);
    }

    static QueryResultBase query(GraphClient graphClient, FuseResourceInfo fuseResourceInfo, Query query)
            throws IOException, InterruptedException {
        return query(graphClient, fuseResourceInfo, query, new CreateGraphCursorRequest());
    }

    static QueryResultBase query(GraphClient graphClient, FuseResourceInfo fuseResourceInfo, int pageSize, Query query)
            throws IOException, InterruptedException {
        return query(graphClient, fuseResourceInfo, query, new CreateGraphCursorRequest(new CreatePageRequest(pageSize)));
    }

    static QueryResultBase query(GraphClient graphClient, FuseResourceInfo fuseResourceInfo, String query, String ontology)
            throws IOException, InterruptedException {
        return query(graphClient, fuseResourceInfo, query,ontology, new CreateGraphCursorRequest());
    }

    static QueryResultBase query(GraphClient graphClient, FuseResourceInfo fuseResourceInfo, int pageSize, String query, String ontology)
            throws IOException, InterruptedException {
        return query(graphClient, fuseResourceInfo, query,ontology, new CreateGraphCursorRequest(new CreatePageRequest(pageSize)));
    }

    static <E,R> QueryResultBase query(GraphClient graphClient, FuseResourceInfo fuseResourceInfo, Query query, CreateCursorRequest createCursorRequest)
            throws IOException, InterruptedException {
        // get Query URL
        QueryResourceInfo queryResourceInfo = graphClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        if(queryResourceInfo.getError()!=null) {
            return new AssignmentsErrorQueryResult(queryResourceInfo.getError());
        }

        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = graphClient.postCursor(queryResourceInfo.getCursorStoreUrl(), createCursorRequest);
        // Press on page to get the relevant page
        PageResourceInfo pageResourceInfo = getPageResourceInfo(graphClient, cursorResourceInfo, createCursorRequest.getCreatePageRequest() != null ? createCursorRequest.getCreatePageRequest().getPageSize() : 1000);
        // return the relevant data
        return graphClient.getPageData(pageResourceInfo.getDataUrl());
    }

    static <E,R> QueryResultBase query(GraphClient graphClient, FuseResourceInfo fuseResourceInfo, String query, String ontology, CreateCursorRequest createCursorRequest)
            throws IOException, InterruptedException {
        // get Query URL
        QueryResourceInfo queryResourceInfo = graphClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query,ontology);
        if(queryResourceInfo.getError()!=null) {
            return new AssignmentsErrorQueryResult(queryResourceInfo.getError());
        }

        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = graphClient.postCursor(queryResourceInfo.getCursorStoreUrl(), createCursorRequest);
        // Press on page to get the relevant page
        PageResourceInfo pageResourceInfo = getPageResourceInfo(graphClient, cursorResourceInfo, createCursorRequest.getCreatePageRequest() != null ? createCursorRequest.getCreatePageRequest().getPageSize() : 1000);
        // return the relevant data
        return graphClient.getPageData(pageResourceInfo.getDataUrl());
    }

    static QueryResultBase nextPage(GraphClient graphClient, CursorResourceInfo cursorResourceInfo, int pageSize) throws IOException, InterruptedException {
        PageResourceInfo pageResourceInfo = getPageResourceInfo(graphClient, cursorResourceInfo, pageSize);
        // return the relevant data
        return graphClient.getPageData(pageResourceInfo.getDataUrl());

    }

    static QueryResultBase nextPage(GraphClient graphClient, CursorResourceInfo cursorResourceInfo, TypeReference typeReference, int pageSize) throws IOException, InterruptedException {
        PageResourceInfo pageResourceInfo = getPageResourceInfo(graphClient, cursorResourceInfo, pageSize);
        // return the relevant data
        return graphClient.getPageData(pageResourceInfo.getDataUrl(),typeReference);

    }

    static PageResourceInfo getPageResourceInfo(GraphClient graphClient, CursorResourceInfo cursorResourceInfo, int pageSize) throws IOException, InterruptedException {
        PageResourceInfo pageResourceInfo = graphClient.postPage(cursorResourceInfo.getPageStoreUrl(),pageSize);
        // Waiting until it gets the response
        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = graphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }
        return pageResourceInfo;
    }

}
