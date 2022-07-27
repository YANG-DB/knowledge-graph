package org.opensearch.graph.client;

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

public interface GraphClientSupport {

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

    static QueryResourceInfo query(GraphClient graphClient, GraphResourceInfo graphResourceInfo, CreateQueryRequest request) throws IOException {
        return graphClient.postQuery(graphResourceInfo.getQueryStoreUrl(),request);
    }

    static QueryResultBase query(GraphClient graphClient, GraphResourceInfo graphResourceInfo, Query query)
            throws IOException, InterruptedException {
        return query(graphClient, graphResourceInfo, query, new CreateGraphCursorRequest());
    }

    static QueryResultBase query(GraphClient graphClient, GraphResourceInfo graphResourceInfo, int pageSize, Query query)
            throws IOException, InterruptedException {
        return query(graphClient, graphResourceInfo, query, new CreateGraphCursorRequest(new CreatePageRequest(pageSize)));
    }

    static QueryResultBase query(GraphClient graphClient, GraphResourceInfo graphResourceInfo, String query, String ontology)
            throws IOException, InterruptedException {
        return query(graphClient, graphResourceInfo, query,ontology, new CreateGraphCursorRequest());
    }

    static QueryResultBase query(GraphClient graphClient, GraphResourceInfo graphResourceInfo, int pageSize, String query, String ontology)
            throws IOException, InterruptedException {
        return query(graphClient, graphResourceInfo, query,ontology, new CreateGraphCursorRequest(new CreatePageRequest(pageSize)));
    }

    static <E,R> QueryResultBase query(GraphClient graphClient, GraphResourceInfo graphResourceInfo, Query query, CreateCursorRequest createCursorRequest)
            throws IOException, InterruptedException {
        // get Query URL
        QueryResourceInfo queryResourceInfo = graphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query);
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

    static <E,R> QueryResultBase query(GraphClient graphClient, GraphResourceInfo graphResourceInfo, String query, String ontology, CreateCursorRequest createCursorRequest)
            throws IOException, InterruptedException {
        // get Query URL
        QueryResourceInfo queryResourceInfo = graphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query,ontology);
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
