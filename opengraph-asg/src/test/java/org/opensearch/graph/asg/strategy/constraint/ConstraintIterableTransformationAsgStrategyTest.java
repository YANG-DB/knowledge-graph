package org.opensearch.graph.asg.strategy.constraint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Supplier;
import org.opensearch.graph.asg.AsgQueryStore;
import org.opensearch.graph.asg.strategy.propertyGrouping.RelPropGroupingAsgStrategy;
import org.opensearch.graph.dispatcher.asg.AsgQuerySupplier;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.*;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.properties.*;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.ePropGroup;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.quant1;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.typed;
import static org.opensearch.graph.model.query.quant.QuantType.all;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

/**
 * Created by benishue on 11-May-17.
 */
public class ConstraintIterableTransformationAsgStrategyTest {
    //region Setup
    @Before
    public void setUp() throws Exception {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("Dragons_Ontology.json");
        StringWriter writer = new StringWriter();
        IOUtils.copy(stream, writer);
        ont = new Ontology.Accessor(new ObjectMapper().readValue(writer.toString(), Ontology.class));

    }

    //endregion
    public static AsgQuery Q1() {
        //region Query Building
        Query query = new Query(); //Person owns Dragon with EProp - Name: 'dragonA'
        query.setOnt("Dragons");
        query.setName("Q1");
        List<EBase> elements = new ArrayList<EBase>();

       /*
        {
          "eNum": 0,
          "type": "Start",
          "next": 1
        }
         */

        Start start = new Start();
        start.seteNum(0);
        start.setNext(1);
        elements.add(start);

       /* Person
         {
          "eNum": 1,
          "type": "ETyped",
          "eTag": "A",
          "eType": 1,
          "next":2
        }
        */

        ETyped eTypedA = new ETyped();
        eTypedA.seteNum(1);
        eTypedA.seteTag("A");
        eTypedA.seteType("Person");
        eTypedA.setNext(2);
        elements.add(eTypedA);

       /* Owns
        {
          "eNum": 2,
          "type": "Rel",
          "rType": 1,
          "dir": "R",
          "next": 3
        }
         */
        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setrType("own");
        rel.setDir(Rel.Direction.R);
        rel.setNext(3);
        elements.add(rel);


       /* Dragon
        {
          "eNum": 3,
          "type": "ETyped",
          "eTag": "B",
          "eType": 2
        }
        */
        ETyped eTypedB = new ETyped();
        eTypedB.seteNum(3);
        eTypedB.seteTag("B");
        eTypedB.seteType("Dragon");
        eTypedB.setNext(4);
        elements.add(eTypedB);

       /* The dragon has the Name Entity Property = "dragonA"
            "type": "EProp",
            "eNum": 4,
            "pType": "name",
            "pTag": "1",
            "con": {
            "op": "eq",
            "expr": "dragonA"
            }
         */


        EProp eProp = new EProp();
        eProp.seteNum(4);
        eProp.setpType("name");
        eProp.setpTag("1");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.eq);
        con.setExpr("dragonA");
        eProp.setCon(con);
        elements.add(eProp);

        query.setElements(elements);

        //endregion

        Supplier<AsgQuery> asgSupplier = new AsgQuerySupplier(query);
        AsgQuery asgQuery = asgSupplier.get();
        return asgQuery;
    }

    //region Test Methods
    @Test
    public void asgConstraintTransformationStrategyEPropsLongToDateTest() throws Exception {
        AsgQuery asgQueryWithEProps = Q1();

        //Setting The EProp expression as a date represented by Long value
        EProp eProp = (EProp) AsgQueryUtil.element(asgQueryWithEProps, 4).get().geteBase();
        eProp.setpType("dateSinceTheBigBang"); //this is a date field - Input is long - epoch time
        eProp.getCon().setOp(ConstraintOp.inSet);
        eProp.getCon().setExpr(new long[]{1000, 205555, 355540, 445450, 587870, 604564, 787481, 8879680, 9798770, 99879891}); //Epoch time as Long

        assertTrue(eProp.getCon().getExpr().getClass().isArray());

        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(ont);
        ConstraintIterableTransformationAsgStrategy constraintIterableTransformationAsgStrategy = new ConstraintIterableTransformationAsgStrategy();

        //Applying the Strategy on the Eprop with the Epoch time
        constraintIterableTransformationAsgStrategy.apply(asgQueryWithEProps, asgStrategyContext);
        Object expr = ((EProp) AsgQueryUtil.element(asgQueryWithEProps, 4).get().geteBase()).getCon().getExpr();
        assertThat(expr, instanceOf(List.class));
    }

    @Test
    public void asgConstraintTransformationCharEscapeTest() throws Exception {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "()*?[&&]]|$%~!{}\\"))))
                .build();

        //Setting The EProp expression as a date represented by Long value

        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(ont);
        ConstraintExpCharEscapeTransformationAsgStrategy constraintIterableTransformationAsgStrategy = new ConstraintExpCharEscapeTransformationAsgStrategy();

        //Applying the Strategy on the Eprop with the Epoch time
        constraintIterableTransformationAsgStrategy.apply(asgQuery, asgStrategyContext);
        final EPropGroup eBase = (EPropGroup) AsgQueryUtil.element(asgQuery, 3).get().geteBase();
        Assert.assertEquals("()*\\?[&&]]|$%~!{}\\\\",eBase.getProps().get(0).getCon().getExpr());
    }

    @Test
    public void asgConstraintTransformationLikeLowecaseTest() throws Exception {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "Hello World"))))
                .build();

        //Setting The EProp expression as a date represented by Long value

        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(ont);
        ConstraintExpLowercaseTransformationAsgStrategy constraintIterableTransformationAsgStrategy
                = new ConstraintExpLowercaseTransformationAsgStrategy(Arrays.asList("firstName","lastName","name"));

        //Applying the Strategy on the Eprop with the Epoch time
        constraintIterableTransformationAsgStrategy.apply(asgQuery, asgStrategyContext);
        final EPropGroup eBase = (EPropGroup) AsgQueryUtil.element(asgQuery, 3).get().geteBase();
        Assert.assertEquals("hello world",eBase.getProps().get(0).getCon().getExpr());
    }

    @Test
    public void asgConstraintTransformationLikeAnyLowecaseTest() throws Exception {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.likeAny, Arrays.asList("Hello","World")))))
                .build();

        //Setting The EProp expression as a date represented by Long value

        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(ont);
        ConstraintExpLowercaseTransformationAsgStrategy constraintIterableTransformationAsgStrategy
                = new ConstraintExpLowercaseTransformationAsgStrategy(Arrays.asList("firstName","lastName","name"));

        //Applying the Strategy on the Eprop with the Epoch time
        constraintIterableTransformationAsgStrategy.apply(asgQuery, asgStrategyContext);
        final EPropGroup eBase = (EPropGroup) AsgQueryUtil.element(asgQuery, 3).get().geteBase();
        Assert.assertTrue(((List) eBase.getProps().get(0).getCon().getExpr()).contains("hello"));
        Assert.assertTrue(((List) eBase.getProps().get(0).getCon().getExpr()).contains("world"));
    }

    @Test
    public void asgConstraintTransformationNoChangeToLowecaseTest() throws Exception {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.likeAny, Arrays.asList("Hello","World")))))
                .build();

        //Setting The EProp expression as a date represented by Long value

        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(ont);
        ConstraintExpLowercaseTransformationAsgStrategy constraintIterableTransformationAsgStrategy
                = new ConstraintExpLowercaseTransformationAsgStrategy(Arrays.asList("firstName","lastName"));

        //Applying the Strategy on the Eprop with the Epoch time
        constraintIterableTransformationAsgStrategy.apply(asgQuery, asgStrategyContext);
        final EPropGroup eBase = (EPropGroup) AsgQueryUtil.element(asgQuery, 3).get().geteBase();
        Assert.assertTrue(((List) eBase.getProps().get(0).getCon().getExpr()).contains("Hello"));
        Assert.assertTrue(((List) eBase.getProps().get(0).getCon().getExpr()).contains("World"));
    }

    @Test
    public void asgConstraintTransformationCharEscapeNestedTest() throws Exception {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.likeAny, Arrays.asList("a", "()*?[&&]]|$%~!{}\\", "b"))), EProp.of(3, "name", Constraint.of(ConstraintOp.like, "***"))))
                .build();

        //Setting The EProp expression as a date represented by Long value

        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(ont);
        ConstraintExpCharEscapeTransformationAsgStrategy constraintIterableTransformationAsgStrategy = new ConstraintExpCharEscapeTransformationAsgStrategy();

        //Applying the Strategy on the Eprop with the Epoch time
        constraintIterableTransformationAsgStrategy.apply(asgQuery, asgStrategyContext);
        final EPropGroup eBase = (EPropGroup) AsgQueryUtil.element(asgQuery, 3).get().geteBase();
        final List expr = (List) eBase.getProps().get(0).getCon().getExpr();
        Assert.assertEquals("a", expr.get(0));
        Assert.assertEquals("()*\\?[&&]]|$%~!{}\\\\",expr.get(1));
        Assert.assertEquals("b",expr.get(2));

        Assert.assertEquals("***", eBase.getProps().get(1).getCon().getExpr());
    }

    @Test
    public void asgConstraintTransformationStrategyRelPropsArrayToListTest() throws Exception {
        AsgQuery asgQueryWithRelProps = AsgQueryStore.Q188_V1();
        AsgQuery asgQueryWithRelPropsOriginal = AsgQueryStore.Q188_V1();

        //region Preparing the Properties for the AsgQuery
        //Setting The RelProp (enum #4) expression as a date represented by Long value
        RelProp rProp1 = (RelProp) AsgQueryUtil.element(asgQueryWithRelProps, 4).get().geteBase();
        rProp1.setpType("dateSinceTheBigBang"); //this is a date field - Input is long type - epoch time
        rProp1.getCon().setExpr(new long[]{10, 20, 30, 40, 50, 60, 71, 80, 90, 91}); //Epoch time as Long
        rProp1.getCon().setOp(ConstraintOp.inRange);
        assertTrue(rProp1.getCon().getExpr().getClass().isArray());

        //Setting The RelProp (enum #5) expression
        RelProp rProp2 = (RelProp) AsgQueryUtil.element(asgQueryWithRelProps, 5).get().geteBase();
        rProp2.setpType("name");
        rProp2.getCon().setExpr(new String[]{"a", "b", "c"});
        rProp2.getCon().setOp(ConstraintOp.inSet);
        assertTrue(rProp2.getCon().getExpr().getClass().isArray());

        RelProp rProp3 = (RelProp) AsgQueryUtil.element(asgQueryWithRelPropsOriginal, 5).get().geteBase();
        rProp3.setpType("dateSinceTheBigBang"); //this is a date field - Input is long type - epoch time
        rProp3.getCon().setExpr(new long[]{10, 20, 30, 40, 50, 60, 71, 80, 90, 91}); //Epoch time as Long
        rProp3.getCon().setOp(ConstraintOp.inRange);
        assertTrue(rProp3.getCon().getExpr().getClass().isArray());

        RelProp rProp4 = (RelProp) AsgQueryUtil.element(asgQueryWithRelPropsOriginal, 4).get().geteBase();
        rProp4.setpType("name");
        rProp4.getCon().setExpr(new String[]{"a", "b", "c"});
        rProp4.getCon().setOp(ConstraintOp.notInSet);
        assertTrue(rProp4.getCon().getExpr().getClass().isArray());

        //endregion

        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(ont);
        ConstraintIterableTransformationAsgStrategy constraintIterableTransformationAsgStrategy = new ConstraintIterableTransformationAsgStrategy();

        //Applying the Strategy on the RelProp #1 with the Epoch time
        constraintIterableTransformationAsgStrategy.apply(asgQueryWithRelProps, asgStrategyContext);
        Object expr1 = ((RelProp) AsgQueryUtil.element(asgQueryWithRelProps, 4).get().geteBase()).getCon().getExpr();
        Object expr2 = ((RelProp) AsgQueryUtil.element(asgQueryWithRelProps, 5).get().geteBase()).getCon().getExpr();
        assertThat(expr1, instanceOf(List.class));
        assertThat(expr2, instanceOf(List.class));
        assertThat(((ArrayList) expr1).get(0), instanceOf(Date.class));
        assertThat(((ArrayList) expr2).get(0), instanceOf(String.class));

        //Appling First the Properties Grouping Startegy and then applying the constraint transformation strategy
        //We want to be sure that the order of strategies is not affecting the final result
        RelPropGroupingAsgStrategy relPropGroupingAsgStrategy = new RelPropGroupingAsgStrategy();
        relPropGroupingAsgStrategy.apply(asgQueryWithRelPropsOriginal, new AsgStrategyContext(null));
        expr1 = ((RelPropGroup) AsgQueryUtil.element(asgQueryWithRelPropsOriginal, 4).get().geteBase()).getProps().get(0).getCon().getExpr();
        expr2 = ((RelPropGroup) AsgQueryUtil.element(asgQueryWithRelPropsOriginal, 4).get().geteBase()).getProps().get(1).getCon().getExpr();
        assertTrue(expr1.getClass().isArray());
        assertTrue(expr2.getClass().isArray());

        //Checking the RelProps grouping mechanism
        AsgEBase<EBase> newRelPropGroupAsgEbase = AsgQueryUtil.element(asgQueryWithRelPropsOriginal, 4).get();
        assertNotNull(newRelPropGroupAsgEbase);

        //Applying again the Constraint Transformation Strategy
        constraintIterableTransformationAsgStrategy.apply(asgQueryWithRelPropsOriginal, asgStrategyContext);
        expr1 = ((RelPropGroup) AsgQueryUtil.element(asgQueryWithRelPropsOriginal, 4).get().geteBase()).getProps().get(0).getCon().getExpr();
        expr2 = ((RelPropGroup) AsgQueryUtil.element(asgQueryWithRelPropsOriginal, 4).get().geteBase()).getProps().get(1).getCon().getExpr();
        assertThat(expr1, instanceOf(List.class));
        assertThat(expr2, instanceOf(List.class));
        assertThat(((ArrayList) expr1).get(0), instanceOf(String.class));
        assertThat(((ArrayList) expr2).get(0), instanceOf(Date.class));

        //Checking first the Constraint Type Transformation and then the Constraint Array Transformation
        AsgQuery asgQueryWithRelProps2 = AsgQueryStore.Q188_V1();
        RelProp rProp5 = (RelProp) AsgQueryUtil.element(asgQueryWithRelProps2, 4).get().geteBase();
        rProp5.setpType("dateSinceTheBigBang"); //this is a date field - Input is long type - epoch time
        rProp5.getCon().setExpr(100L); //Epoch time as Long
        rProp5.getCon().setOp(ConstraintOp.ge);
        assertThat(rProp5.getCon().getExpr(), instanceOf(Long.class));

        //Setting The RelProp (enum #5) expression
        RelProp rProp6 = (RelProp) AsgQueryUtil.element(asgQueryWithRelProps2, 5).get().geteBase();
        rProp6.setpType("name");
        rProp6.getCon().setExpr(new String[]{"a", "b", "c"});
        rProp6.getCon().setOp(ConstraintOp.inRange);
        assertTrue(rProp6.getCon().getExpr().getClass().isArray());

        ConstraintTypeTransformationAsgStrategy asgConstraintTypeTransformationStrategy = new ConstraintTypeTransformationAsgStrategy();
        asgConstraintTypeTransformationStrategy.apply(asgQueryWithRelProps2, asgStrategyContext);
        asgConstraintTypeTransformationStrategy.apply(asgQueryWithRelProps2, asgStrategyContext); //dsecond call to the same strategy

        Object expr5 = ((RelProp) AsgQueryUtil.element(asgQueryWithRelProps2, 4).get().geteBase()).getCon().getExpr();
        assertThat(expr5, instanceOf(Date.class));


        //Lets call now to the Constraint Array Transformation
        ConstraintIterableTransformationAsgStrategy constraintIterableTransformationAsgStrategy1 = new ConstraintIterableTransformationAsgStrategy();
        constraintIterableTransformationAsgStrategy1.apply(asgQueryWithRelProps2, asgStrategyContext);

        Object expr3 = ((RelProp) AsgQueryUtil.element(asgQueryWithRelProps2, 4).get().geteBase()).getCon().getExpr();
        Object expr4 = ((RelProp) AsgQueryUtil.element(asgQueryWithRelProps2, 5).get().geteBase()).getCon().getExpr();
        assertThat(expr3, instanceOf(Date.class));
        assertThat(expr4, instanceOf(List.class));


        RelProp rProp7 = (RelProp) AsgQueryUtil.element(asgQueryWithRelProps2, 5).get().geteBase();
        rProp6.setpType("dateSinceTheBigBang");
        rProp6.getCon().setExpr(new long[]{212121, 555557, 987654321});
        rProp6.getCon().setOp(ConstraintOp.inRange);
        assertTrue(rProp6.getCon().getExpr().getClass().isArray());
        constraintIterableTransformationAsgStrategy1.apply(asgQueryWithRelProps2, asgStrategyContext);

        Object expr8 = ((RelProp) AsgQueryUtil.element(asgQueryWithRelProps2, 5).get().geteBase()).getCon().getExpr();
        assertThat(expr8, instanceOf(List.class));
        assertThat(((ArrayList) expr8).get(0), instanceOf(Date.class));
    }
    //endregion

    //region Fields
    private Ontology.Accessor ont;
    //endregion
}