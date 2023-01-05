package org.opensearch.graph.assembly.queries;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.graph.assembly.Setup;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.Start;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.quant.Quant1;
import org.opensearch.graph.model.query.quant.QuantType;
import org.opensearch.graph.model.resourceInfo.GraphResourceInfo;
import org.opensearch.graph.model.resourceInfo.ResultResourceInfo;
import org.opensearch.graph.model.results.*;
import org.opensearch.graph.test.BaseITMarker;
import org.opensearch.action.admin.indices.refresh.RefreshRequest;
import org.junit.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static org.opensearch.graph.assembly.Setup.GraphClient;
import static org.opensearch.graph.client.GraphClientSupport.query;

public class DragonsSimpleNoConstraintsQueryIT implements BaseITMarker {
    public static final String DRAGONS = "Dragons";
    static private SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DEFAULT_DATE_FORMAT);

    @BeforeClass
    public static void setup() throws Exception {
//        Setup.setup(); //todo remove remark when running IT tests
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @AfterClass
    public static void after() {
//        Setup.cleanup();
    }

    @Test
    public void testLoadLogicalGraph() throws IOException, URISyntaxException {
        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        Assert.assertNotNull(graphResourceInfo);

        initIndices();
        Map map;

        URL stream = Thread.currentThread().getContextClassLoader().getResource("schema/LogicalDragonsGraph.json");
        ResultResourceInfo<String> info = GraphClient.uploadGraphFile(DRAGONS, stream);
        Assert.assertFalse(info.isError());
        //refresh cluster
        Setup.client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();

        map = (Map) new ObjectMapper().readValue(info.getResult(), Map.class).get("data");
        Assert.assertFalse(map.isEmpty());
        Assert.assertEquals(2,((List)map.get("responses")).size());
        Assert.assertNotNull(((Map)((List)map.get("responses")).get(0)).get("successes"));
        Assert.assertEquals(64,((List)((Map)((List)map.get("responses")).get(0)).get("successes")).size());
        Assert.assertNotNull(((Map)((List)map.get("responses")).get(1)).get("successes"));
        Assert.assertEquals(64,((List)((Map)((List)map.get("responses")).get(1)).get("successes")).size());
    }

    private void initIndices() throws IOException {
        Map map = new ObjectMapper().readValue(GraphClient.initIndices(DRAGONS), Map.class);
        Assert.assertEquals(map.get("data").toString().trim(), "indices created:19");

        //refresh cluster
        Setup.client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();
    }

    @Test
    public void testPersonKnowsPerson() throws IOException, InterruptedException, URISyntaxException {
        // Create Ontology query to fetch newly created entity
        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        Assert.assertNotNull(graphResourceInfo);

        initIndices();

        URL stream = Thread.currentThread().getContextClassLoader().getResource("schema/LogicalDragonsGraph.json");
        ResultResourceInfo<String> info = GraphClient.uploadGraphFile(DRAGONS, stream);
        Assert.assertFalse(info.isError());
        //refresh cluster
        Setup.client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();

        Query query = Query.Builder.instance().withName("query").withOnt(DRAGONS)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "p1", "Person", 2, 0),
                        new Rel(2, "Know", Rel.Direction.R, "k",3),
                        new ETyped(3, "p2", "Person", 0, 0)
                )).build();
        QueryResultBase pageData = query(GraphClient, graphResourceInfo, 1000, query);

        Assert.assertEquals(1,((AssignmentsQueryResult)pageData).getAssignments().size());
        Assert.assertEquals(3,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getEntities().size());
        Assert.assertEquals(3,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getRelationships().size());

    }

    @Test
    public void testPersonWithNestedProfessionKnowsPerson() throws IOException, InterruptedException, URISyntaxException {
        // Create Ontology query to fetch newly created entity
        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        Assert.assertNotNull(graphResourceInfo);

        initIndices();

        URL stream = Thread.currentThread().getContextClassLoader().getResource("schema/LogicalDragonsGraph.json");
        ResultResourceInfo<String> info = GraphClient.uploadGraphFile(DRAGONS, stream);
        Assert.assertFalse(info.isError());
        //refresh cluster
        Setup.client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();

        Query query = Query.Builder.instance().withName("query").withOnt(DRAGONS)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "p1", "Person", 2, 0),
                        new Rel(2, "Know", Rel.Direction.R, "k",3),
                        new ETyped(3, "p2", "Person", 0, 0)
                )).build();
        QueryResultBase pageData = query(GraphClient, graphResourceInfo, 1000, query);

        Assert.assertEquals(1,((AssignmentsQueryResult)pageData).getAssignments().size());
        Assert.assertEquals(3,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getEntities().size());
        Assert.assertEquals(3,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getRelationships().size());

    }

    @Test
    public void testPersonOwnsDragonAndKnowsPerson() throws IOException, InterruptedException, URISyntaxException {
        // Create Ontology query to fetch newly created entity
        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        Assert.assertNotNull(graphResourceInfo);

        initIndices();

        URL stream = Thread.currentThread().getContextClassLoader().getResource("schema/LogicalDragonsGraph.json");
        ResultResourceInfo<String> info = GraphClient.uploadGraphFile(DRAGONS, stream);
        Assert.assertFalse(info.isError());
        //refresh cluster
        Setup.client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();

        Query query = Query.Builder.instance().withName("query").withOnt(DRAGONS)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "p1", "Person", 100, 0),
                        new Quant1(100, QuantType.all,Arrays.asList(2,4)),
                        new Rel(2, "Know", Rel.Direction.R, "know",3),
                        new ETyped(3, "p2", "Person", 0, 0),
                        new Rel(4, "Own", Rel.Direction.R, "own",5),
                        new ETyped(5, "d2", "Dragon", 0, 0)
                )).build();
        QueryResultBase pageData = query(GraphClient, graphResourceInfo, 1000, query);

        Assert.assertEquals(1,((AssignmentsQueryResult)pageData).getAssignments().size());
        Assert.assertEquals(6,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getEntities().size());
        Assert.assertEquals(5,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getRelationships().size());

        Assert.assertEquals(3,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getRelationships().stream().filter(e->((Relationship)e).getrType().equals("Own")).count());
        Assert.assertEquals(2,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getRelationships().stream().filter(e->((Relationship)e).getrType().equals("Know")).count());

        Assert.assertEquals(3,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getEntities().stream().filter(e->((Entity)e).geteType().equals("Dragon")).count());
        Assert.assertEquals(3,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getEntities().stream().filter(e->((Entity)e).geteType().equals("Person")).count());

    }

    @Test
    public void testDragonOwnedByPerson() throws IOException, InterruptedException, URISyntaxException {
        // Create Ontology query to fetch newly created entity
        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        Assert.assertNotNull(graphResourceInfo);

        initIndices();

        URL stream = Thread.currentThread().getContextClassLoader().getResource("schema/LogicalDragonsGraph.json");
        ResultResourceInfo<String> info = GraphClient.uploadGraphFile(DRAGONS, stream);
        Assert.assertFalse(info.isError());
        //refresh cluster
        Setup.client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();

        Query query = Query.Builder.instance().withName("query").withOnt(DRAGONS)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "d1", "Dragon", 100, 0),
                        new Quant1(100, QuantType.all,Arrays.asList(2)),
                        new Rel(2, "Own", Rel.Direction.L, "own",3),
                        new ETyped(3, "p1", "Person", 0, 0)
                )).build();
        QueryResultBase pageData = query(GraphClient, graphResourceInfo, 1000, query);

        Assert.assertEquals(1,((AssignmentsQueryResult)pageData).getAssignments().size());
        Assert.assertEquals(5,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getEntities().size());
        Assert.assertEquals(3,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getRelationships().size());

        Assert.assertEquals(3,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getRelationships().stream().filter(e->((Relationship)e).getrType().equals("Own")).count());

        Assert.assertEquals(3,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getEntities().stream().filter(e->((Entity)e).geteType().equals("Dragon")).count());
        Assert.assertEquals(2,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getEntities().stream().filter(e->((Entity)e).geteType().equals("Person")).count());

    }

    @Test
    public void testPersonOwnsDragon() throws IOException, InterruptedException, URISyntaxException {
        // Create Ontology query to fetch newly created entity
        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        Assert.assertNotNull(graphResourceInfo);

        initIndices();

        URL stream = Thread.currentThread().getContextClassLoader().getResource("schema/LogicalDragonsGraph.json");
        ResultResourceInfo<String> info = GraphClient.uploadGraphFile(DRAGONS, stream);
        Assert.assertFalse(info.isError());
        //refresh cluster
        Setup.client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();

        Query query = Query.Builder.instance().withName("query").withOnt(DRAGONS)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "p1", "Person", 100, 0),
                        new Quant1(100, QuantType.all,Arrays.asList(2)),
                        new Rel(2, "Own", Rel.Direction.R, "own",3),
                        new ETyped(3, "d1", "Dragon", 0, 0)
                )).build();
        QueryResultBase pageData = query(GraphClient, graphResourceInfo, 1000, query);

        Assert.assertEquals(1,((AssignmentsQueryResult)pageData).getAssignments().size());
        Assert.assertEquals(5,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getEntities().size());
        Assert.assertEquals(3,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getRelationships().size());

        Assert.assertEquals(3,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getRelationships().stream().filter(e->((Relationship)e).getrType().equals("Own")).count());

        Assert.assertEquals(3,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getEntities().stream().filter(e->((Entity)e).geteType().equals("Dragon")).count());
        Assert.assertEquals(2,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getEntities().stream().filter(e->((Entity)e).geteType().equals("Person")).count());

    }

    @Test
    public void testDragonOwnsPerson() throws IOException, InterruptedException, URISyntaxException {
        // Create Ontology query to fetch newly created entity
        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        Assert.assertNotNull(graphResourceInfo);

        initIndices();

        URL stream = Thread.currentThread().getContextClassLoader().getResource("schema/LogicalDragonsGraph.json");
        ResultResourceInfo<String> info = GraphClient.uploadGraphFile(DRAGONS, stream);
        Assert.assertFalse(info.isError());
        //refresh cluster
        Setup.client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();

        Query query = Query.Builder.instance().withName("query").withOnt(DRAGONS)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "d1", "Dragon", 100, 0),
                        new Quant1(100, QuantType.all,Arrays.asList(2)),
                        new Rel(2, "Own", Rel.Direction.R, "own",3),
                        new ETyped(3, "p1", "Person", 0, 0)
                )).build();
        QueryResultBase pageData = query(GraphClient, graphResourceInfo, 1000, query);

        Assert.assertTrue(pageData instanceof AssignmentsErrorQueryResult);
        Assert.assertEquals("Query",((AssignmentsErrorQueryResult)pageData).error().getErrorCode());
    }

    @Test
    @Ignore("Works fine with NonRedundant, fails since it finds vertices in DiscreteVertexController.canDoWithoutQuery even without" +
            "finding the proper Person.BOTH.Dragon schema ")
    public void testPersonOwnsBothDirectionsDragon() throws IOException, InterruptedException, URISyntaxException {
        // Create Ontology query to fetch newly created entity
        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        Assert.assertNotNull(graphResourceInfo);

        initIndices();

        URL stream = Thread.currentThread().getContextClassLoader().getResource("schema/LogicalDragonsGraph.json");
        ResultResourceInfo<String> info = GraphClient.uploadGraphFile(DRAGONS, stream);
        Assert.assertFalse(info.isError());
        //refresh cluster
        Setup.client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();

        Query query = Query.Builder.instance().withName("query").withOnt(DRAGONS)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "p1", "Person", 100, 0),
                        new Quant1(100, QuantType.all,Arrays.asList(2)),
                        new Rel(2, "Own", Rel.Direction.RL, "own",3),
                        new ETyped(3, "d1", "Dragon", 0, 0)
                )).build();
        QueryResultBase pageData = query(GraphClient, graphResourceInfo, 1000, query);

        Assert.assertEquals(1,((AssignmentsQueryResult)pageData).getAssignments().size());
        Assert.assertEquals(5,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getEntities().size());
        Assert.assertEquals(3,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getRelationships().size());

        Assert.assertEquals(3,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getRelationships().stream().filter(e->((Relationship)e).getrType().equals("Own")).count());

        Assert.assertEquals(3,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getEntities().stream().filter(e->((Entity)e).geteType().equals("Dragon")).count());
        Assert.assertEquals(2,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getEntities().stream().filter(e->((Entity)e).geteType().equals("Person")).count());

    }

    @Test
    public void testPersonOwnsDragonOrKnowsPerson() throws IOException, InterruptedException, URISyntaxException {
        // Create Ontology query to fetch newly created entity
        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        Assert.assertNotNull(graphResourceInfo);

        initIndices();

        URL stream = Thread.currentThread().getContextClassLoader().getResource("schema/LogicalDragonsGraph.json");
        ResultResourceInfo<String> info = GraphClient.uploadGraphFile(DRAGONS, stream);
        Assert.assertFalse(info.isError());
        //refresh cluster
        Setup.client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();

        Query query = Query.Builder.instance().withName("query").withOnt(DRAGONS)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "p1", "Person", 100, 0),
                        new Quant1(100, QuantType.some,Arrays.asList(2,4)),
                        new Rel(2, "Know", Rel.Direction.R, "know",3),
                        new ETyped(3, "p2", "Person", 0, 0),
                        new Rel(4, "Own", Rel.Direction.R, "own",5),
                        new ETyped(5, "d2", "Dragon", 0, 0)
                )).build();
        QueryResultBase pageData = query(GraphClient, graphResourceInfo, 1000, query);

        Assert.assertEquals(1,((AssignmentsQueryResult)pageData).getAssignments().size());
        Assert.assertEquals(6,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getEntities().size());
        Assert.assertEquals(6,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getRelationships().size());

        Assert.assertEquals(3,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getRelationships().stream().filter(e->((Relationship)e).getrType().equals("Own")).count());
        Assert.assertEquals(3,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getRelationships().stream().filter(e->((Relationship)e).getrType().equals("Know")).count());

        Assert.assertEquals(3,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getEntities().stream().filter(e->((Entity)e).geteType().equals("Dragon")).count());
        Assert.assertEquals(3,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getEntities().stream().filter(e->((Entity)e).geteType().equals("Person")).count());

    }
}
