package org.opensearch.graph.services.engine2.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.graph.client.BaseGraphClient;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.*;
import org.opensearch.graph.model.query.entity.EConcrete;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.RelProp;
import org.opensearch.graph.model.query.quant.Quant1;
import org.opensearch.graph.model.query.quant.QuantType;
import org.opensearch.graph.model.resourceInfo.*;
import org.opensearch.graph.model.results.AssignmentsQueryResult;
import org.opensearch.graph.model.transport.PlanTraceOptions;
import org.opensearch.graph.client.GraphClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.TimeZone;

import static org.opensearch.graph.model.OntologyTestUtils.*;

/**
 * Created by Roman on 07/06/2017.
 */
@Ignore
public class RealClusterE2ET {
    @Before
    public void setup() throws IOException {
        //GraphClient = new GraphClient("http://40.118.108.95:8888/opengraph");
        GraphClient = new BaseGraphClient("http://localhost:8888/opengraph");
        //GraphClient = new GraphClient("http://localhost:8888/opengraph");
        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        $ont = new Ontology.Accessor(GraphClient.getOntology(graphResourceInfo.getCatalogStoreUrl() + "/Dragons"));
    }

    @Test
    @Ignore
    public void test1() throws IOException, InterruptedException {
        Query query = Query.Builder.instance().withName("Q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$(DRAGON.name), 2, 0),
                new Rel(2, $ont.rType$(FIRE.getName()), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", $ont.eType$(DRAGON.name), 0, 0)
        )).build();

        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }

    @Test
    @Ignore
    public void test_EntityRelEntityWithFilters() throws IOException, InterruptedException {
        Query query = Query.Builder.instance().withName("Q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$(DRAGON.name), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                new EProp(3, NAME.type, Constraint.of(ConstraintOp.eq, "reagan")),
                new Rel(4, $ont.rType$(FIRE.getName()), Rel.Direction.R, null, 5, 0),
                new ETyped(5, "B", $ont.eType$(DRAGON.name), 6, 0),
                new EProp(6, NAME.type, Constraint.of(ConstraintOp.eq, "erwin"))
        )).build();

        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }


    @Test
    @Ignore
    public void test2() throws IOException, InterruptedException {
        Query query = Query.Builder.instance().withName("Q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$(DRAGON.name), 2, 0),
                new Rel(2, $ont.rType$(FREEZE.getName()), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", $ont.eType$(DRAGON.name), 0, 0)
        )).build();

        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }

    @Test
    @Ignore
    public void test3() throws IOException, InterruptedException {
        Query query = Query.Builder.instance().withName("Q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$(DRAGON.name), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                new EProp(3, NAME.type, Constraint.of(ConstraintOp.eq, "lenora")),
                new Rel(4, $ont.rType$(FIRE.getName()), Rel.Direction.R, null, 5, 0),
                new ETyped(5, "B", $ont.eType$(DRAGON.name), 0, 0)
        )).build();

        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query, PlanTraceOptions.of(PlanTraceOptions.Level.verbose));
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), 100);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }

    @Test
    @Ignore
    public void test4() throws IOException, InterruptedException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DEFAULT_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Query query = Query.Builder.instance().withName("Q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$(DRAGON.name), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                new EProp(3, NAME.type, Constraint.of(ConstraintOp.eq, "lenora")),
                new Rel(4, $ont.rType$(FIRE.getName()), Rel.Direction.R, null, 6, 5),
                new RelProp(5, TIMESTAMP.type, Constraint.of(ConstraintOp.inRange,
                        Arrays.asList(sdf.parse("2000-04-05 00:00:00.000").getTime(), sdf.parse("2000-05-05 00:00:00.000").getTime())), 0),
                new ETyped(6, "B", $ont.eType$(DRAGON.name), 7, 0),
                new EProp(7, NAME.type, Constraint.of(ConstraintOp.eq, "gideon"))
        )).build();

        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), 100);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }

    /**
     * Plan: {plan=[11:12:9:10:6:8:4:5:1:3], estimation=EntityOp(Asg(ETyped(11))):EntityFilterOp(Asg(EPropGroup(12))):RelationOp(Asg(Rel(9))):RelationFilterOp(Asg(RelPropGroup(10))):EntityOp(Asg(ETyped(6))):EntityFilterOp(Asg(EPropGroup(8))):RelationOp(Asg(Rel(4))):RelationFilterOp(Asg(RelPropGroup(5))):EntityOp(Asg(ETyped(1))):EntityFilterOp(Asg(EPropGroup(3))) >> DoubleCost{estimation=1000000.0}}
     * Traversal: [GraphStep(vertex,[])@[C], HasStep([constraint.eq(Constraint.by([HasStep([~label.eq(Dragon)])])), name.raw(name), power.raw(power)]), VertexStep(OUT,[promise],edge)@[C-->B], HasStep([constraint.eq(Constraint.by([AndStep([[HasStep([~label.eq(freeze)])], [HasStep([direction.eq(OUT)])], [HasStep([startDate.lt(Sat Jul 01 03:00:00 IDT 2000)])], [HasStep([entityB.type.within([Dragon])])], [HasStep([entityB.name.eq(gideon)])]])]))]), EdgeOtherVertexStep, VertexStep(OUT,[promiseFilter],edge), HasStep([name.raw(name)]), EdgeOtherVertexStep@[B], VertexStep(OUT,[promise],edge)@[B-->A], HasStep([constraint.eq(Constraint.by([AndStep([[HasStep([~label.eq(fire)])], [HasStep([direction.eq(IN)])], [HasStep([timestamp.and(gte(Wed Apr 05 02:00:00 IST 2000), lt(Fri May 05 03:00:00 IDT 2000))])], [HasStep([entityB.type.within([Dragon])])], [HasStep([entityB.name.eq(lenora)])]])]))]), EdgeOtherVertexStep, VertexStep(OUT,[promiseFilter],edge), HasStep([name.raw(name)]), EdgeOtherVertexStep@[A], PathStep]
     * ES-Query:
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws ParseException
     */
    @Test
    @Ignore
    public void test5() throws IOException, InterruptedException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DEFAULT_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Query query = Query.Builder.instance().withName("Q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$(DRAGON.name), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                new EProp(3, NAME.type, Constraint.of(ConstraintOp.eq, "lenora")),
                new Rel(4, $ont.rType$(FIRE.getName()), Rel.Direction.R, null, 6, 5),
                new RelProp(5, TIMESTAMP.type, Constraint.of(ConstraintOp.inRange,
                        Arrays.asList(sdf.parse("2000-04-05 00:00:00.000").getTime(), sdf.parse("2000-05-05 00:00:00.000").getTime())), 0),
                new ETyped(6, "B", $ont.eType$(DRAGON.name), 7, 0),
                new Quant1(7, QuantType.all, Arrays.asList(8, 9), 0),
                new EProp(8, NAME.type, Constraint.of(ConstraintOp.eq, "gideon")),
                new Rel(9, FREEZE.getrType(), Rel.Direction.L, null, 11, 10),
                new RelProp(10, START_DATE.type,
                        Constraint.of(ConstraintOp.lt, sdf.parse("2000-07-01 00:00:00.000").getTime()), 0),
                new ETyped(11, "C", $ont.eType$(DRAGON.name), 0, 0)
        )).build();

        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());

        long start = System.currentTimeMillis();
        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), 100);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());
        Assert.assertNotNull(actualAssignmentsQueryResult);
        long elapsed = System.currentTimeMillis() - start;
//        System.out.println(elapsed);
    }

    @Test
    @Ignore
    public void test5_2() throws IOException, InterruptedException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DEFAULT_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Query query = Query.Builder.instance().withName("Q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "C", $ont.eType$(DRAGON.name), 2, 0),
                new Rel(2, FREEZE.getrType(), Rel.Direction.R, null, 4, 3),
                new RelProp(3, START_DATE.type,
                        Constraint.of(ConstraintOp.lt, sdf.parse("2000-07-01 00:00:00.000").getTime()), 0),
                new ETyped(4, "B", $ont.eType$(DRAGON.name), 5, 0),
                new Quant1(5, QuantType.all, Arrays.asList(6, 7), 0),
                new EProp(6, NAME.type, Constraint.of(ConstraintOp.eq, "gideon")),
                new Rel(7, $ont.rType$(FIRE.getName()), Rel.Direction.L, null, 9, 8),
                new RelProp(8, TIMESTAMP.type, Constraint.of(ConstraintOp.inRange,
                        Arrays.asList(sdf.parse("2000-04-05 00:00:00.000").getTime(), sdf.parse("2000-05-05 00:00:00.000").getTime())), 0),
                new ETyped(9, "A", $ont.eType$(DRAGON.name), 10, 0),
                new EProp(10, NAME.type, Constraint.of(ConstraintOp.eq, "lenora"))
        )).build();

        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());

        long start = System.currentTimeMillis();
        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), 100);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());
        Assert.assertNotNull(actualAssignmentsQueryResult);

        long elapsed = System.currentTimeMillis() - start;
//        System.out.println(elapsed);
    }


    @Test
    @Ignore
    public void test5_with_no_props() throws IOException, InterruptedException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DEFAULT_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Query query = Query.Builder.instance().withName("Q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$(DRAGON.name), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                new EProp(3, NAME.type, Constraint.of(ConstraintOp.eq, "lenora")),
                new Rel(4, $ont.rType$(FIRE.getName()), Rel.Direction.R, null, 6, 5),
                new RelProp(5, TIMESTAMP.type, Constraint.of(ConstraintOp.inRange,
                        Arrays.asList(sdf.parse("2000-04-05 00:00:00.000").getTime(), sdf.parse("2000-05-05 00:00:00.000").getTime())), 0),
                new ETyped(6, "B", $ont.eType$(DRAGON.name), 7, 0),
                new Quant1(7, QuantType.all, Arrays.asList(8, 9), 0),
                new EProp(8, NAME.type, Constraint.of(ConstraintOp.eq, "gideon")),
                new Rel(9, FREEZE.getrType(), Rel.Direction.L, null, 11, 10),
                new RelProp(10, START_DATE.type,
                        Constraint.of(ConstraintOp.lt, sdf.parse("2000-07-01 00:00:00.000").getTime()), 0),
                new ETyped(11, "C", $ont.eType$(DRAGON.name), 0, 0)
        )).build();

        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());

        long start = System.currentTimeMillis();
        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), 100);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());
        Assert.assertNotNull(actualAssignmentsQueryResult);

        long elapsed = System.currentTimeMillis() - start;
//        System.out.println(elapsed);
    }

    @Test
    @Ignore
    public void test6() throws IOException, InterruptedException, ParseException {
        Query query = Query.Builder.instance().withName("Q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", PERSON.type, 2, 0),
                new Rel(2, OWN.getrType(), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", DRAGON.type, 4, 0),
                new Rel(4, FREEZE.getrType(), Rel.Direction.R, null, 5, 0),
                new ETyped(5, "C", DRAGON.type, 6, 0),
                new Rel(6, FREEZE.getrType(), Rel.Direction.R, null, 7, 0),
                new ETyped(7, "D", DRAGON.type, 0, 0)))
                .build();

        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), 100);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }

    @Test
    @Ignore
    public void test7() throws IOException, InterruptedException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DEFAULT_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Query query = Query.Builder.instance().withName("Q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", PERSON.type, 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                new EProp(3, HEIGHT.type, Constraint.of(ConstraintOp.inRange, new int[]{200, 205})),
                new Rel(4, OWN.getrType(), Rel.Direction.R, null, 6, 5),
                new RelProp(5, START_DATE.type, Constraint.of(ConstraintOp.inRange,
                        Arrays.asList(sdf.parse("2000-01-01 00:00:00.000").getTime(), sdf.parse("2000-12-30 00:00:00.000").getTime())), 0),
                new ETyped(6, "B", DRAGON.type, 7, 0),
                new Quant1(7, QuantType.all, Arrays.asList(8, 9, 12), 0),
                new EProp(8, POWER.type, Constraint.of(ConstraintOp.inRange, new int[]{25, 75})),
                new Rel(9, FREEZE.getrType(), Rel.Direction.R, null, 11, 10),
                new RelProp(10, START_DATE.type,
                        Constraint.of(ConstraintOp.lt, sdf.parse("2000-07-01 00:00:00.000").getTime()), 0),
                new ETyped(11, "C", DRAGON.type, 0, 0),
                new Rel(12, FIRE.getrType(), Rel.Direction.L, null, 15, 13),
                new RelProp(13, TIMESTAMP.type,
                        Constraint.of(ConstraintOp.gt, sdf.parse("2000-07-01 00:00:00.000").getTime()), 0),
                new ETyped(15, "D", DRAGON.type, 0, 0)))
                .build();

        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());

        long start = System.currentTimeMillis();
        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), 100);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());
        Assert.assertNotNull(actualAssignmentsQueryResult);

        long elapsed = System.currentTimeMillis() - start;
//        System.out.println(elapsed);
    }

    @Test
    @Ignore
    public void test8() throws IOException, InterruptedException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DEFAULT_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Query query = Query.Builder.instance().withName("Q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", DRAGON.type, 2, 0),
                new Rel(2, OWN.getrType(), Rel.Direction.L, null, 3, 0),
                new ETyped(3, "B", PERSON.type, 0, 0)))
                .build();

        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());

        long start = System.currentTimeMillis();
        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), 100);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());
        Assert.assertNotNull(actualAssignmentsQueryResult);

        long elapsed = System.currentTimeMillis() - start;
//        System.out.println(elapsed);
    }

    @Test
    @Ignore
    public void test9() throws IOException, InterruptedException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DEFAULT_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Query query = Query.Builder.instance().withName("Q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", PERSON.type, 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 5), 0),
                new Rel(3, OWN.getrType(), Rel.Direction.R, null, 4, 0),
                new ETyped(4, "B", DRAGON.type, 0, 0),
                new Rel(5, OWN.getrType(), Rel.Direction.R, null, 6, 0),
                new ETyped(6, "C", HORSE.type, 0, 0)))
                .build();

        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());

        long start = System.currentTimeMillis();
        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), 100);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());
        Assert.assertNotNull(actualAssignmentsQueryResult);

        long elapsed = System.currentTimeMillis() - start;
//        System.out.println(elapsed);
    }

    @Test
    @Ignore
    public void test10() throws IOException, InterruptedException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DEFAULT_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Query query = Query.Builder.instance().withName("2sw3sq").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "Sy7SLI6GZ", PERSON.type, 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(5, 6), 0),
                new ETyped(3, "SJBS88TMƒ", DRAGON.type, 0, 0),
                new ETyped(4, "SJwrUIpGW", HORSE.type, 0, 0),
                new Rel(5, OWN.getrType(), Rel.Direction.R, null, 3, 0),
                new Rel(6, OWN.getrType(), Rel.Direction.R, null, 4, 0)))
                .build();

        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query, "BkIIIUaMZ", query.getName());
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());

        long start = System.currentTimeMillis();
        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), 100);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());
        Assert.assertNotNull(actualAssignmentsQueryResult);

        long elapsed = System.currentTimeMillis() - start;
//        System.out.println(elapsed);
    }

    @Test
    @Ignore
    public void test11() throws IOException, InterruptedException, ParseException {
        Query query = Query.Builder.instance().withName("Q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", PERSON.type, 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3), 0),
                new EProp(3, HEIGHT.type, Constraint.of(ConstraintOp.ge, 200))))
                .build();

        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), 100);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }

    @Test
    @Ignore
    public void test12() throws IOException, InterruptedException, ParseException {
        Query query = Query.Builder.instance().withName("Q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", PERSON.type, 2, 0),
                new Rel(2, OWN.getrType(), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", DRAGON.type, 4, 0),
                new EProp(4, COLOR.type, Constraint.of(ConstraintOp.eq, "RED"))))
                .build();

        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), 100);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }

    @Test
    @Ignore
    public void test13() throws IOException, InterruptedException, ParseException {
        Query query = Query.Builder.instance().withName("Q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", PERSON.type, 2, 0),
                new EProp(2, HEIGHT.type, Constraint.of(ConstraintOp.eq, 200))))
                .build();

        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), 100);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());
        String plan = GraphClient.getPlan(queryResourceInfo.getExplainPlanUrl());
        int x = 5;

    }

    @Test
    @Ignore
    public void test14() throws IOException, InterruptedException {
        String queryString = "{\n" +
                "    \"elements\": [\n" +
                "      {\n" +
                "        \"eNum\": 0,\n" +
                "        \"type\": \"Start\",\n" +
                "        \"next\": 1\n" +
                "      },\n" +
                "      {\n" +
                "        \"eNum\": 1,\n" +
                "        \"next\": 2,\n" +
                "        \"eType\": 1,\n" +
                "        \"eTag\": \"Sk6lKtCzb\",\n" +
                "        \"type\": \"ETyped\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"eNum\": 2,\n" +
                "        \"next\": [\n" +
                "          10,\n" +
                "          11\n" +
                "        ],\n" +
                "        \"qType\": \"all\",\n" +
                "        \"type\": \"Quant1\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"eNum\": 3,\n" +
                "        \"next\": 5,\n" +
                "        \"eType\": 2,\n" +
                "        \"eTag\": \"Hyy-FKCz-\",\n" +
                "        \"type\": \"ETyped\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"eNum\": 4,\n" +
                "        \"next\": 15,\n" +
                "        \"eType\": 1,\n" +
                "        \"eTag\": \"H1XmFY0fW\",\n" +
                "        \"type\": \"ETyped\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"eNum\": 5,\n" +
                "        \"next\": [\n" +
                "          12,\n" +
                "          13\n" +
                "        ],\n" +
                "        \"qType\": \"all\",\n" +
                "        \"type\": \"Quant1\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"eNum\": 6,\n" +
                "        \"eType\": 2,\n" +
                "        \"eTag\": \"r1VbKFCGZ\",\n" +
                "        \"type\": \"ETyped\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"eNum\": 7,\n" +
                "        \"next\": 14,\n" +
                "        \"eType\": 2,\n" +
                "        \"eTag\": \"B1fWYYAf-\",\n" +
                "        \"type\": \"ETyped\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"eNum\": 8,\n" +
                "        \"eType\": 3,\n" +
                "        \"eTag\": \"Hkr7FK0MZ\",\n" +
                "        \"type\": \"ETyped\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"eNum\": 9,\n" +
                "        \"eType\": 2,\n" +
                "        \"eTag\": \"HJ17Yt0fZ\",\n" +
                "        \"type\": \"ETyped\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"eNum\": 10,\n" +
                "        \"type\": \"Rel\",\n" +
                "        \"rType\": 101,\n" +
                "        \"dir\": \"R\",\n" +
                "        \"next\": 3\n" +
                "      },\n" +
                "      {\n" +
                "        \"eNum\": 11,\n" +
                "        \"type\": \"Rel\",\n" +
                "        \"rType\": 108,\n" +
                "        \"dir\": \"R\",\n" +
                "        \"next\": 4\n" +
                "      },\n" +
                "      {\n" +
                "        \"eNum\": 12,\n" +
                "        \"type\": \"Rel\",\n" +
                "        \"rType\": 103,\n" +
                "        \"dir\": \"R\",\n" +
                "        \"next\": 7\n" +
                "      },\n" +
                "      {\n" +
                "        \"eNum\": 13,\n" +
                "        \"type\": \"Rel\",\n" +
                "        \"rType\": 104,\n" +
                "        \"dir\": \"R\",\n" +
                "        \"next\": 6\n" +
                "      },\n" +
                "      {\n" +
                "        \"eNum\": 14,\n" +
                "        \"type\": \"Rel\",\n" +
                "        \"rType\": 103,\n" +
                "        \"dir\": \"R\",\n" +
                "        \"next\": 9\n" +
                "      },\n" +
                "      {\n" +
                "        \"eNum\": 15,\n" +
                "        \"type\": \"Rel\",\n" +
                "        \"rType\": 101,\n" +
                "        \"dir\": \"R\",\n" +
                "        \"next\": 8\n" +
                "      }\n" +
                "    ],\n" +
                "    \"name\": \"3ptuam\",\n" +
                "    \"ont\": \"Dragons\"\n" +
                "  }";

        Query query = new ObjectMapper().readValue(queryString, Query.class);

        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), 10);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }

    @Test
    @Ignore
    public void test15() throws IOException, InterruptedException {
        String queryString = "{\"name\": \"3ptuam\",\n" +
                "\"ont\": \"Dragons\",\n" +
                "\"elements\":[\n" +
                "\t{\"eNum\":0,\"type\":\"Start\",\"next\":1},\n" +
                "\t{\"eNum\":1,\"next\":2,\"eType\":2,\"eTag\":\"D1\",\"type\":\"ETyped\"},\n" +
                "\t{\"eNum\":2,\"next\":[3,4,5,15],\"qType\":\"all\",\"type\":\"Quant1\"},\n" +
                "\t{\"eNum\":3,\"type\":\"EProp\",\"pType\":\"power\",\"con\":{\"op\":\"gt\",\"expr\":\"80\"}},\n" +
                "\t{\"eNum\":4,\"type\":\"EProp\",\"pType\":\"gender\",\"con\":{\"op\":\"eq\",\"expr\":\"MALE\"}},\n" +
                "\t{\"eNum\":5,\"type\":\"EProp\",\"pType\":\"color\",\"con\":{\"op\":\"ne\",\"expr\":\"RED\"}},\n" +
                "\t{\"eNum\":6,\"next\":7,\"eType\":2,\"eTag\":\"D2\",\"type\":\"ETyped\"},\n" +
                "\t{\"eNum\":7,\"next\":[9,10,11,17],\"qType\":\"all\",\"type\":\"Quant1\"},\n" +
                "\t{\"eNum\":8,\"next\":18,\"eType\":5,\"eTag\":\"K\",\"type\":\"ETyped\"},\n" +
                "\t{\"eNum\":9,\"type\":\"EProp\",\"pType\":\"gender\",\"con\":{\"op\":\"eq\",\"expr\":\"FEMALE\"}},\n" +
                "\t{\"eNum\":10,\"type\":\"EProp\",\"pType\":\"power\",\"con\":{\"op\":\"lt\",\"expr\":\"80\"}},\n" +
                "\t{\"eNum\":11,\"type\":\"EProp\",\"pType\":\"color\",\"con\":{\"op\":\"eq\",\"expr\":\"RED\"}},\n" +
                "\t{\"eNum\":12,\"next\":19,\"eType\":1,\"eTag\":\"P\",\"type\":\"ETyped\"},\n" +
                "\t{\"eNum\":13,\"eType\":3,\"eTag\":\"H\",\"type\":\"ETyped\",\"next\":14},\n" +
                "\t{\"eNum\":14,\"next\":[20,21],\"qType\":\"all\",\"type\":\"Quant1\"},\n" +
                "\t{\"eNum\":20,\"type\":\"EProp\",\"pType\":\"name\",\"con\":{\"op\":\"eq\",\"expr\":\"molly\"}},\n" +
                "\t{\"eNum\":21,\"type\":\"EProp\",\"pType\":\"name\",\"con\":{\"op\":\"eq\",\"expr\":\"cotter\"}},\n" +
                "\t{\"eNum\":16,\"type\":\"RelProp\",\"pType\":\"temperature\",\"con\":{\"op\":\"gt\",\"expr\":\"100\"}},\n" +
                "\t{\"eNum\":15,\"type\":\"Rel\",\"rType\":103,\"dir\":\"R\",\"next\":6,\"b\":16},\n" +
                "\t{\"eNum\":17,\"type\":\"Rel\",\"rType\":105,\"dir\":\"R\",\"next\":8},\n" +
                "\t{\"eNum\":18,\"type\":\"Rel\",\"rType\":106,\"dir\":\"L\",\"next\":12},\n" +
                "\t{\"eNum\":19,\"type\":\"Rel\",\"rType\":101,\"dir\":\"R\",\"next\":13}\n" +
                "\t]}";

        Query query = new ObjectMapper().readValue(queryString, Query.class);

        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), 10);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }

    @Test
    @Ignore
    public void test16() throws IOException, InterruptedException, ParseException {
        Query query = Query.Builder.instance().withName("Q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", PERSON.type, 2, 0),
                new Rel(2, OWN.getrType(), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "H", HORSE.type, 4, 0),
                new Quant1(4, QuantType.all, Arrays.asList(5, 6), 0),
                new EProp(5, NAME.type, Constraint.of(ConstraintOp.eq, "molly")),
                new EProp(6, NAME.type, Constraint.of(ConstraintOp.eq, "cotter"))))
                .build();

        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), 100);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }

    @Test
    @Ignore
    public void test17() throws IOException, InterruptedException, ParseException {
        Query query = Query.Builder.instance().withName("Q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new EConcrete(1, "A", PERSON.type, "5", "5", 2, 0),
                new Rel(2, KNOW.getrType(), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "H", PERSON.type, 0, 0)))
                .build();

        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());

        long start = System.currentTimeMillis();

        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), 100);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }

    @Test
    @Ignore
    public void testShimon1() throws IOException, InterruptedException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DEFAULT_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        String queryJson = "{\"ont\":\"Dragons\",\"name\":\"query14\",\"elements\":[{\"eNum\":0,\"type\":\"Start\",\"next\":1},{\"eNum\":1,\"next\":2,\"eType\":5,\"eTag\":\"SJlTeTCMb\",\"type\":\"ETyped\"},{\"eNum\":2,\"next\":[3,10],\"qType\":\"all\",\"type\":\"Quant1\"},{\"eNum\":3,\"type\":\"EProp\",\"pType\":7,\"con\":{\"op\":\"eq\",\"expr\":\"chroyane\"}},{\"eNum\":4,\"next\":5,\"eType\":1,\"eTag\":\"rkHala0fb\",\"type\":\"ETyped\"},{\"eNum\":5,\"next\":[11,12],\"qType\":\"all\",\"type\":\"Quant1\"},{\"eNum\":6,\"eType\":4,\"eTag\":\"HJ4imaRGZ\",\"type\":\"ETyped\",\"next\":8},{\"eNum\":7,\"eType\":2,\"eTag\":\"SJ6KVT0GZ\",\"type\":\"ETyped\",\"next\":9},{\"eNum\":8,\"type\":\"EProp\",\"pType\":7,\"con\":{\"op\":\"eq\",\"expr\":\"estren\"}},{\"eNum\":9,\"type\":\"EProp\",\"pType\":14,\"con\":{\"op\":\"gt\",\"expr\":\"50\"}},{\"eNum\":10,\"type\":\"Rel\",\"rType\":106,\"dir\":\"L\",\"next\":4},{\"eNum\":11,\"type\":\"Rel\",\"rType\":102,\"dir\":\"R\",\"next\":6},{\"eNum\":12,\"type\":\"Rel\",\"rType\":101,\"dir\":\"R\",\"next\":7}]}";
        Query query = new ObjectMapper().readValue(queryJson, Query.class);

        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());

        long start = System.currentTimeMillis();

        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), 100);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }

    //region Fields
    private static GraphClient GraphClient;
    private static Ontology.Accessor $ont;
    //endregion
}
