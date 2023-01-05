package org.opensearch.graph.assembly.queries;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.graph.assembly.Setup;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.Start;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.RelProp;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.opensearch.graph.model.query.properties.projection.IdentityProjection;
import org.opensearch.graph.model.query.quant.Quant1;
import org.opensearch.graph.model.query.quant.QuantType;
import org.opensearch.graph.model.resourceInfo.GraphResourceInfo;
import org.opensearch.graph.model.resourceInfo.ResultResourceInfo;
import org.opensearch.graph.model.results.*;
import org.opensearch.action.admin.indices.refresh.RefreshRequest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensearch.graph.test.BaseITMarker;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.TimeZone;

import static org.opensearch.graph.assembly.Setup.GraphClient;
import static org.opensearch.graph.client.GraphClientSupport.query;

public class DragonsSimpleConstraintsQueryIT implements BaseITMarker {
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
    public void testPersonKnowsPersonProjection() throws IOException, InterruptedException, URISyntaxException {
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
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4)),
                        new EProp(3, "firstName", new IdentityProjection()),
                        new Rel(4, "Know", Rel.Direction.R, "k", 5),
                        new ETyped(5, "p2", "Person", 6, 0),
                        new EProp(6, "firstName", new IdentityProjection())
                )).build();
        QueryResultBase pageData = query(GraphClient, graphResourceInfo, 1000, query);

        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());
        Assert.assertEquals(3, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getEntities().size());
        Assert.assertEquals(3, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getRelationships().size());

        //validate projection only returned the single explicit firstName field
        Assignment<Entity, Relationship> assignment = (Assignment<Entity, Relationship>) ((AssignmentsQueryResult) pageData).getAssignments().get(0);
        assignment.getEntities().forEach(e-> {
            Assert.assertEquals(1,e.getProperties().size());
            Assert.assertEquals("firstName",e.getProperties().iterator().next().getpType());
        });
        Assert.assertNotNull(AssignmentDescriptor.printGraph(assignment));

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
                        new Quant1(2, QuantType.all, Arrays.asList(3, 5)),
                        new Rel(3, "Know", Rel.Direction.R, "k", 4),
                        new ETyped(4, "p2", "Person", 0, 0),
                        new EProp(5, "firstName", Constraint.of(ConstraintOp.eq, "Napoleon"))
                )).build();
        QueryResultBase pageData = query(GraphClient, graphResourceInfo, 1000, query);

        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());
        Assignment assignment = (Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0);
        Assert.assertEquals(2, assignment.getEntities().size());
        Assert.assertEquals(1, assignment.getRelationships().size());
        Assert.assertNotNull(AssignmentDescriptor.printGraph(assignment));

    }

    private void initIndices() throws IOException {
        Map map = new ObjectMapper().readValue(GraphClient.initIndices(DRAGONS), Map.class);
        Assert.assertEquals(map.get("data").toString().trim(), "indices created:19");
        //refresh cluster
        Setup.client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();
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
                        new Quant1(100, QuantType.all, Arrays.asList(2, 4)),
                        new Rel(2, "Know", Rel.Direction.R, "know", 3),
                        new ETyped(3, "p2", "Person", 0, 0),
                        new Rel(4, "Own", Rel.Direction.R, "own", 5),
                        new ETyped(5, "d2", "Dragon", 6, 0),
                        new EProp(6, "power", Constraint.of(ConstraintOp.gt, 100))
                )).build();
        QueryResultBase pageData = query(GraphClient, graphResourceInfo, 1000, query);

        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());
        Assignment assignment = (Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0);
        Assert.assertEquals(3, assignment.getEntities().size());
        Assert.assertEquals(2, assignment.getRelationships().size());

        Assert.assertEquals(1, assignment.getRelationships().stream().filter(e -> ((Relationship) e).getrType().equals("Own")).count());
        Assert.assertEquals(1, assignment.getRelationships().stream().filter(e -> ((Relationship) e).getrType().equals("Know")).count());

        Assert.assertEquals(1, assignment.getEntities().stream().filter(e -> ((Entity) e).geteType().equals("Dragon")).count());
        Assert.assertEquals(2, assignment.getEntities().stream().filter(e -> ((Entity) e).geteType().equals("Person")).count());
        Assert.assertNotNull(AssignmentDescriptor.printGraph(assignment));

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
                        new Quant1(100, QuantType.all, Arrays.asList(2)),
                        new Rel(2, "Own", Rel.Direction.L, "own", 3,20),
                        new RelProp(20,"startDate",Constraint.of(ConstraintOp.gt,"1793-04-13 22:00:00.000")),
                        new ETyped(3, "p1", "Person", 0, 0)
                )).build();
        QueryResultBase pageData = query(GraphClient, graphResourceInfo, 1000, query);

        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());
        Assignment assignment = (Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0);
        Assert.assertEquals(2, assignment.getEntities().size());
        Assert.assertEquals(1, assignment.getRelationships().size());

        Assert.assertEquals(1, assignment.getRelationships().stream().filter(e -> ((Relationship) e).getrType().equals("Own")).count());

        Assert.assertEquals(1, assignment.getEntities().stream().filter(e -> ((Entity) e).geteType().equals("Dragon")).count());
        Assert.assertEquals(1, assignment.getEntities().stream().filter(e -> ((Entity) e).geteType().equals("Person")).count());
        Assert.assertNotNull(AssignmentDescriptor.printGraph(assignment));

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
                        new Quant1(100, QuantType.all, Arrays.asList(2)),
                        new Rel(2, "Own", Rel.Direction.R, "own", 3,20),
                        new RelProp(20,"startDate",Constraint.of(ConstraintOp.gt,"1793-04-13 22:00:00.000")),
                        new ETyped(3, "d1", "Dragon", 0, 0)
                )).build();
        QueryResultBase pageData = query(GraphClient, graphResourceInfo, 1000, query);

        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());
        Assignment assignment = (Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0);
        Assert.assertEquals(2, assignment.getEntities().size());
        Assert.assertEquals(1, assignment.getRelationships().size());

        Assert.assertEquals(1, assignment.getRelationships().stream().filter(e -> ((Relationship) e).getrType().equals("Own")).count());

        Assert.assertEquals(1, assignment.getEntities().stream().filter(e -> ((Entity) e).geteType().equals("Dragon")).count());
        Assert.assertEquals(1, assignment.getEntities().stream().filter(e -> ((Entity) e).geteType().equals("Person")).count());
        Assert.assertNotNull(AssignmentDescriptor.printGraph(assignment));

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
                        new Quant1(100, QuantType.some, Arrays.asList(2, 4)),
                        new Rel(2, "Know", Rel.Direction.R, "know", 3),
                            new ETyped(3, "p2", "Person", 30, 0),
                            new EProp(30, "firstName", Constraint.of(ConstraintOp.eq, "Napoleon")),
                        new Rel(4, "Own", Rel.Direction.R, "own", 5),
                            new ETyped(5, "d2", "Dragon", 50, 0),
                            new EProp(50, "birthDate", Constraint.of(ConstraintOp.gt, "600-01-01 22:00:00.000"))
                )).build();
        QueryResultBase pageData = query(GraphClient, graphResourceInfo, 1000, query);

        Assignment assignment = (Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0);
        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());
        Assert.assertEquals(5, assignment.getEntities().size());
        Assert.assertEquals(3, assignment.getRelationships().size());

        Assert.assertEquals(2, assignment.getRelationships().stream().filter(e -> ((Relationship) e).getrType().equals("Own")).count());
        Assert.assertEquals(1, assignment.getRelationships().stream().filter(e -> ((Relationship) e).getrType().equals("Know")).count());

        Assert.assertEquals(2, assignment.getEntities().stream().filter(e -> ((Entity) e).geteType().equals("Dragon")).count());
        Assert.assertEquals(3, assignment.getEntities().stream().filter(e -> ((Entity) e).geteType().equals("Person")).count());

        Assert.assertNotNull(AssignmentDescriptor.printGraph(assignment));


    }
}
