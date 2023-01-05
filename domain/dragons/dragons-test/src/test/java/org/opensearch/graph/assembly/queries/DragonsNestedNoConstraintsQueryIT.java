package org.opensearch.graph.assembly.queries;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.graph.assembly.Setup;
import org.opensearch.graph.client.BaseGraphClient;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.Start;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
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

public class DragonsNestedNoConstraintsQueryIT implements BaseITMarker {
    public static final String DRAGONS = "Dragons";
    static private SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DEFAULT_DATE_FORMAT);

    @BeforeClass
    public static void setup() throws Exception {
//        Setup.setup();//todo remove remark when running IT tests
        GraphClient = new BaseGraphClient("http://localhost:8888/opengraph");
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

//        initIndices();

        URL stream = Thread.currentThread().getContextClassLoader().getResource("schema/LogicalDragonsGraphWithNested.json");
        ResultResourceInfo<String> info = GraphClient.uploadGraphFile(DRAGONS, stream);
        Assert.assertFalse(info.isError());
        //refresh cluster
//        Setup.client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();

        Map map = (Map) new ObjectMapper().readValue(info.getResult(), Map.class).get("data");
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
    @Ignore("Code under construction for supporting nested structure")
    public void testPersonWithName() throws IOException, InterruptedException, URISyntaxException {
        // Create Ontology query to fetch newly created entity
        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        Assert.assertNotNull(graphResourceInfo);

//        initIndices();

        URL stream = Thread.currentThread().getContextClassLoader().getResource("schema/LogicalDragonsGraphWithNested.json");
        ResultResourceInfo<String> info = GraphClient.uploadGraphFile(DRAGONS, stream);
        Assert.assertFalse(info.isError());
        //refresh cluster
//        Setup.client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();

        Query query = Query.Builder.instance().withName("query").withOnt(DRAGONS)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "p1", "Person", 2, 0),
                        new Rel(2, "HasProfession", Rel.Direction.R, "k",3),
                        new ETyped(3, "pr", "Profession", 0, 0)
                )).build();
        QueryResultBase pageData = query(GraphClient, graphResourceInfo, 1000, query);

        Assert.assertEquals(1,((AssignmentsQueryResult)pageData).getAssignments().size());
        Assert.assertEquals(3,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getEntities().size());
        Assert.assertEquals(3,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getRelationships().size());

    }

    @Test
    public void testPersonHasProfession() throws IOException, InterruptedException, URISyntaxException {
        // Create Ontology query to fetch newly created entity
        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        Assert.assertNotNull(graphResourceInfo);

//        initIndices();

        URL stream = Thread.currentThread().getContextClassLoader().getResource("schema/LogicalDragonsGraphWithNested.json");
        ResultResourceInfo<String> info = GraphClient.uploadGraphFile(DRAGONS, stream);
        Assert.assertFalse(info.isError());
        //refresh cluster
//        Setup.client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();

        Query query = Query.Builder.instance().withName("query").withOnt(DRAGONS)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "p1", "Person", 2, 0),
                        new EProp(2, "firstName", Constraint.of(ConstraintOp.eq, "Napoleon"))
                )).build();
        QueryResultBase pageData = query(GraphClient, graphResourceInfo, 1000, query);

        Assert.assertEquals(1,((AssignmentsQueryResult)pageData).getAssignments().size());
        Assert.assertEquals(1,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getEntities().size());
        Assert.assertEquals(0,((Assignment)((AssignmentsQueryResult)pageData).getAssignments().get(0)).getRelationships().size());

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


}
