package org.opensearch.graph.services.engine2.data;

import javaslang.collection.Stream;
import org.junit.*;
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
import org.opensearch.graph.services.TestsConfiguration;
import org.opensearch.graph.test.BaseITMarker;
import org.opensearch.graph.test.framework.index.MappingEngineConfigurer;
import org.opensearch.graph.test.framework.index.Mappings;
import org.opensearch.graph.test.framework.index.Mappings.Mapping;
import org.opensearch.graph.test.framework.index.Mappings.Mapping.Property;
import org.opensearch.graph.test.framework.index.SearchEmbeddedNode;
import org.opensearch.graph.test.framework.populator.SearchEngineDataPopulator;

import java.io.IOException;
import java.util.*;

import static org.opensearch.graph.test.framework.index.Mappings.Mapping.Property.Type.keyword;

/**
 * Created by Roman on 12/04/2017.
 */
public class SingleEntityIT implements BaseITMarker {
    @BeforeClass
    public static void setup() throws Exception {
        GraphClient = new BaseGraphClient("http://localhost:8888/fuse");

        String idField = "id";

        TransportClient client = SearchEmbeddedNode.getClient();

        new MappingEngineConfigurer("person", new Mappings().addMapping("pge",
                new Mapping().addProperty("type", new Property(keyword))
                    .addProperty("name", new Property(keyword)))).configure(client);

        new SearchEngineDataPopulator(
                client,
                "person",
                "pge",
                idField,
                () -> createPeople(10)).populate();


        new MappingEngineConfigurer("dragon", new Mappings().addMapping("pge",
                new Mapping().addProperty("type", new Property(keyword))
                        .addProperty("name", new Property(keyword)))).configure(client);
        new SearchEngineDataPopulator(
                client,
                "dragon",
                "pge",
                idField,
                () -> createDragons(10)).populate();

        client.admin().indices().refresh(new RefreshRequest("person", "dragon")).actionGet();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        SearchEmbeddedNode.getClient().admin().indices()
                .delete(new DeleteIndexRequest("person", "dragon")).actionGet();
    }

    @Before
    public void before() {
        Assume.assumeTrue(TestsConfiguration.instance.shouldRunTestClass(this.getClass()));
    }

    //region TestMethods
    @Test
    public void test_PeopleQuery_SingleAssignment() throws IOException, InterruptedException {
        testSinglePageResult("People", "Dragons", "Person", 1, 1, Optional.empty());
    }

    @Test
    public void test_PeopleQuery_MultipleAssignments() throws IOException, InterruptedException {
        testSinglePageResult("People", "Dragons", "Person", 10, 10,
                Optional.of(Arrays.asList("p0", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9")));
    }

    @Test
    public void test_PeopleQuery_MultipleAssignments_MoreThanExists() throws IOException, InterruptedException {
        testSinglePageResult("People", "Dragons", "Person", 20, 10,
                Optional.of(Arrays.asList("p0", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9")));
    }

    @Test
    public void test_DragonsQuery_SingleAssignment() throws IOException, InterruptedException {
        testSinglePageResult("Dragons", "Dragons", "Dragon", 1, 1, Optional.empty());
    }

    @Test
    public void test_DragonsQuery_MultipleAssignments() throws IOException, InterruptedException {
        testSinglePageResult("Dragons", "Dragons", "Dragon", 10, 10,
                Optional.of(Arrays.asList("d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9")));
    }

    @Test
    public void test_DragonsQuery_MultipleAssignments_MoreThanExists() throws IOException, InterruptedException {
        testSinglePageResult("Dragons", "Dragons", "Dragon", 20, 10,
                Optional.of(Arrays.asList("d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9")));
    }

    @Test
    public void test_PeopleQuery_MultipleAssignments_MultiplePageResults_PageSize1() throws IOException, InterruptedException {
        testMultiplePageResults("People", "Dragons", "Person", 1,
                Arrays.asList("p0", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9"));
    }

    @Test
    public void test_PeopleQuery_MultipleAssignments_MultiplePageResults_PageSize2() throws IOException, InterruptedException {
        testMultiplePageResults("People", "Dragons", "Person", 2,
                Arrays.asList("p0", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9"));
    }

    @Test
    public void test_PeopleQuery_MultipleAssignments_MultiplePageResults_PageSize5() throws IOException, InterruptedException {
        testMultiplePageResults("People", "Dragons", "Person", 5,
                Arrays.asList("p0", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9"));
    }

    @Test
    public void test_DragonsQuery_MultipleAssignments_MultiplePageResults_PageSize1() throws IOException, InterruptedException {
        testMultiplePageResults("Dragons", "Dragons", "Dragon", 1,
                Arrays.asList("d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9"));
    }

    @Test
    public void test_DragonsQuery_MultipleAssignments_MultiplePageResults_PageSize2() throws IOException, InterruptedException {
        testMultiplePageResults("Dragons", "Dragons", "Dragon", 2,
                Arrays.asList("d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9"));
    }

    @Test
    public void test_DragonsQuery_MultipleAssignments_MultiplePageResults_PageSize5() throws IOException, InterruptedException {
        testMultiplePageResults("Dragons", "Dragons", "Dragon", 5,
                Arrays.asList("d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9"));
    }
    //endregion

    //region TestHelper Methods
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

    protected void testMultiplePageResults(
            String queryName,
            String ontologyName,
            String eType,
            int pageSize,
            Collection<String> expectedIds
    ) throws IOException, InterruptedException {

        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), createSimpleEntityQuery(queryName, ontologyName, eType));
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());

        Set<String> ids = new HashSet<>();
        for(int i = 0 ; i < (10 / pageSize) ; i++) {
            PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), pageSize);
            while (!pageResourceInfo.isAvailable()) {
                pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
                if (!pageResourceInfo.isAvailable()) {
                    Thread.sleep(10);
                }
            }

            Assert.assertTrue(pageResourceInfo.getRequestedPageSize() == pageSize);
            Assert.assertTrue(pageResourceInfo.getActualPageSize() == pageSize);

            AssignmentsQueryResult pageData = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());
            List<Assignment<Entity,Relationship>> assignments = pageData.getAssignments();
            Assert.assertTrue(assignments.size() == pageSize);
            assignments.forEach(assignment -> {
                Assert.assertTrue(assignment.getEntities().size() == 1);
                ids.add(assignment.getEntities().get(0).geteID());

                Assert.assertTrue(assignment.getEntities().get(0).geteTag().size() == 1);
                Assert.assertTrue(Stream.ofAll(assignment.getEntities().get(0).geteTag()).get(0).equals("A"));
                Assert.assertTrue(assignment.getEntities().get(0).geteType().equals(eType));
            });
        }

        Assert.assertTrue(ids.size() == expectedIds.size());
        Assert.assertTrue(ids.containsAll(expectedIds));

        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), pageSize);
        AssignmentsQueryResult pageData = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());

        Assert.assertTrue(pageResourceInfo.getRequestedPageSize() == pageSize);
        Assert.assertTrue(pageResourceInfo.getActualPageSize() == 0);
        Assert.assertTrue(pageData.getAssignments() == null || pageData.getAssignments().size() == 0);
    }
    //endregion

    //region Protected Methods
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

    protected static Iterable<Map<String, Object>> createPeople(int numPeople) {
        List<Map<String, Object>> people = new ArrayList<>();
        for(int i = 0 ; i < numPeople ; i++) {
            Map<String, Object> person = new HashMap<>();
            person.put("id", "p" + i);
            person.put("type", "Person");
            person.put("name", "person" + i);
            people.add(person);
        }
        return people;
    }

    protected static Iterable<Map<String, Object>> createDragons(int numDragons) {
        List<Map<String, Object>> dragons = new ArrayList<>();
        for(int i = 0 ; i < numDragons ; i++) {
            Map<String, Object> dragon = new HashMap<>();
            dragon.put("id", "d" + i);
            dragon.put("type", "Dragon");
            dragon.put("name", "dragon" + i);
            dragons.add(dragon);
        }
        return dragons;
    }
    //endregion

    //region Fields
    private static SearchEmbeddedNode elasticEmbeddedNode;
    private static GraphClient GraphClient;
    //endregion
}
