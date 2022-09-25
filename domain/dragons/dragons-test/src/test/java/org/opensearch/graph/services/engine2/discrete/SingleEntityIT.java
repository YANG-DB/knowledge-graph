package org.opensearch.graph.services.engine2.discrete;

import javaslang.collection.Stream;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.action.admin.indices.refresh.RefreshRequest;
import org.opensearch.client.transport.TransportClient;
import org.opensearch.graph.client.BaseGraphClient;
import org.opensearch.graph.client.GraphClient;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.query.Start;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.resourceInfo.CursorResourceInfo;
import org.opensearch.graph.model.resourceInfo.GraphResourceInfo;
import org.opensearch.graph.model.resourceInfo.PageResourceInfo;
import org.opensearch.graph.model.resourceInfo.QueryResourceInfo;
import org.opensearch.graph.model.results.Assignment;
import org.opensearch.graph.model.results.AssignmentsQueryResult;
import org.opensearch.graph.model.results.Entity;
import org.opensearch.graph.model.results.Relationship;
import org.opensearch.graph.test.BaseITMarker;
import org.opensearch.graph.test.framework.index.MappingEngineConfigurer;
import org.opensearch.graph.test.framework.index.Mappings;
import org.opensearch.graph.test.framework.index.SearchEmbeddedNode;
import org.opensearch.graph.test.framework.populator.SearchEngineDataPopulator;

import java.io.IOException;
import java.util.*;

import static org.opensearch.graph.test.framework.index.Mappings.Mapping.Property.Type.keyword;

/**
 * Created by roman.margolis on 02/10/2017.
 */
public class SingleEntityIT implements BaseITMarker {
    //region setup
    @BeforeClass
    public static void setup() throws Exception {
        GraphClient = new BaseGraphClient("http://localhost:8888/opengraph");

        String idField = "id";

        TransportClient client = SearchEmbeddedNode.getClient();

        new MappingEngineConfigurer(Arrays.asList("person1", "person2"), new Mappings().addMapping("pge",
                new Mappings.Mapping().addProperty("type", new Mappings.Mapping.Property(keyword))
                        .addProperty("name", new Mappings.Mapping.Property(keyword)))).configure(client);

        new SearchEngineDataPopulator(
                client,
                "person1",
                "pge",
                idField,
                true,
                null,
                false,
                () -> createPeople(0, 5)).populate();

        new SearchEngineDataPopulator(
                client,
                "person2",
                "pge",
                idField,
                true,
                null,
                false,
                () -> createPeople(5, 10)).populate();

        client.admin().indices().refresh(new RefreshRequest("person1", "person2")).actionGet();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        SearchEmbeddedNode.getClient().admin().indices()
                .delete(new DeleteIndexRequest("person1", "person2")).actionGet();
    }
    //endregion

    //region Tests
    @Test
    public void test_PeopleQuery_SingleAssignment() throws IOException, InterruptedException {
        testSinglePageResult("People", "Dragons", "Person", 1, 1, Optional.empty());
    }
    //endregion

    //region Protected Methods
    protected void testSinglePageResult(
            String queryName,
            String ontologyName,
            String eType,
            int requestedPageSize,
            int actualPageSize,
            Optional<Collection<String>> expectedIds
    ) throws IOException, InterruptedException {

        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), createSimpleEntityQuery(queryName, ontologyName, eType));
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), requestedPageSize);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult pageData = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());

        Assert.assertEquals(requestedPageSize, pageResourceInfo.getRequestedPageSize());
        Assert.assertEquals(actualPageSize, pageResourceInfo.getActualPageSize());
        List<Assignment<Entity,Relationship>> assignments = pageData.getAssignments();
        Assert.assertEquals(actualPageSize, assignments.size());

        Set<String> ids = new HashSet<>();
        assignments.forEach(assignment -> {
            Assert.assertTrue(assignment.getEntities().size() == 1);
            ids.add(assignment.getEntities().get(0).geteID());

            Assert.assertTrue(assignment.getEntities().get(0).geteTag().size() == 1);
            Assert.assertTrue(Stream.ofAll(assignment.getEntities().get(0).geteTag()).get(0).equals("A"));
            Assert.assertTrue(assignment.getEntities().get(0).geteType().equals(eType));
        });

        if (expectedIds.isPresent()) {
            Assert.assertTrue(ids.size() == expectedIds.get().size());
            Assert.assertTrue(ids.containsAll(expectedIds.get()));
        }
    }

    protected Query createSimpleEntityQuery(String queryName, String ontologyName, String entityType) {
        Start start = new Start();
        start.seteNum(0);
        start.setNext(1);

        ETyped eTyped = new ETyped();
        eTyped.seteType(entityType);
        eTyped.seteTag("A");
        eTyped.seteNum(1);

        return Query.Builder.instance()
                .withName(queryName)
                .withOnt(ontologyName)
                .withElements(Arrays.asList(start, eTyped))
                .build();
    }

    protected static Iterable<Map<String, Object>> createPeople(int startId, int endId) {
        List<Map<String, Object>> people = new ArrayList<>();
        for(int i = startId ; i < endId ; i++) {
            Map<String, Object> person = new HashMap<>();
            person.put("id", "p" + String.format("%03d", i));
            person.put("type", "Person");
            person.put("name", "person" + i);
            people.add(person);
        }
        return people;
    }
    //endregion

    //region Fields
    private static GraphClient GraphClient;
    //endregion
}
