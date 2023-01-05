package org.opensearch.graph.asg.translator.cypher.strategies;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Supplier;
import org.opensearch.graph.asg.strategy.constraint.ConstraintTypeTransformationAsgStrategy;
import org.opensearch.graph.asg.strategy.propertyGrouping.RelPropGroupingAsgStrategy;
import org.opensearch.graph.asg.AsgQueryStore;
import org.opensearch.graph.dispatcher.asg.AsgQuerySupplier;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.Start;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.RelProp;
import org.opensearch.graph.model.query.properties.RelPropGroup;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Created by benishue on 09-May-17.
 */
public class MatchTransformationStrategyTest {
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
        rel.setrType("Person");
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
            "pType": "1.1",
            "pTag": "1",
            "con": {
            "op": "eq",
            "expr": "dragonA"
            }
         */


        EProp eProp = new EProp();
        eProp.seteNum(4);
        eProp.setpType("1");
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
        eProp.getCon().setExpr(123456L); //Epoch time as Long

        assertThat(eProp.getCon().getExpr(), instanceOf(Long.class));

        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(ont);
        ConstraintTypeTransformationAsgStrategy asgConstraintTypeTransformationStrategy = new ConstraintTypeTransformationAsgStrategy();

        //Applying the Strategy on the Eprop with the Epoch time
        asgConstraintTypeTransformationStrategy.apply(asgQueryWithEProps, asgStrategyContext);
        Object expr = ((EProp) AsgQueryUtil.element(asgQueryWithEProps, 4).get().geteBase()).getCon().getExpr();
        assertThat(expr, instanceOf(Date.class));
    }

    @Test
    public void asgConstraintTransformationStrategyEPropsIntToDateTest() throws Exception {
        AsgQuery asgQueryWithEProps = Q1();

        //Setting The EProp expression as a date represented by Long value
        EProp eProp = (EProp) AsgQueryUtil.element(asgQueryWithEProps, 4).get().geteBase();
        eProp.setpType("dateSinceTheBigBang"); //this is a date field - Input is long - epoch time
        eProp.getCon().setExpr(123456); //Epoch time as Long

        assertThat(eProp.getCon().getExpr(), instanceOf(int.class));

        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(ont);
        ConstraintTypeTransformationAsgStrategy asgConstraintTypeTransformationStrategy = new ConstraintTypeTransformationAsgStrategy();

        //Applying the Strategy on the Eprop with the Epoch time
        asgConstraintTypeTransformationStrategy.apply(asgQueryWithEProps, asgStrategyContext);
        Object expr = ((EProp) AsgQueryUtil.element(asgQueryWithEProps, 4).get().geteBase()).getCon().getExpr();
        assertThat(expr, instanceOf(Date.class));
    }

    @Test
    public void asgConstraintTransformationStrategyEPropsIntToLongTest() throws Exception {
        AsgQuery asgQueryWithEProps = Q1();

        //Setting The EProp expression as an int
        EProp eProp = (EProp) AsgQueryUtil.element(asgQueryWithEProps, 4).get().geteBase();
        eProp.setpType("height"); //this is an int field
        eProp.getCon().setExpr(99);

        assertThat(eProp.getCon().getExpr(), instanceOf(Integer.class));


        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(ont);
        ConstraintTypeTransformationAsgStrategy asgConstraintTypeTransformationStrategy = new ConstraintTypeTransformationAsgStrategy();

        //Applying the Strategy on the Eprop with the Epoch time
        asgConstraintTypeTransformationStrategy.apply(asgQueryWithEProps, asgStrategyContext);
        Object expr = ((EProp) AsgQueryUtil.element(asgQueryWithEProps, 4).get().geteBase()).getCon().getExpr();
        assertThat(expr, instanceOf(long.class));
    }

    @Test
    public void asgConstraintTransformationStrategyRelPropsLongToDateTest() throws Exception {
        AsgQuery asgQueryWithRelProps = AsgQueryStore.Q188();
        AsgQuery asgQueryWithRelPropsOriginal = AsgQueryStore.Q188();

        //region Preparing the Properties for the AsgQuery
        //Setting The RelProp (enum #4) expression as a date represented by Long value
        RelProp rProp1 = (RelProp) AsgQueryUtil.element(asgQueryWithRelProps, 4).get().geteBase();
        rProp1.setpType("dateSinceTheBigBang"); //this is a date field - Input is long type - epoch time
        rProp1.getCon().setExpr(123456L); //Epoch time as Long
        assertThat(rProp1.getCon().getExpr(), instanceOf(Long.class));

        //Setting The RelProp (enum #5) expression as a date represented by Long value
        RelProp rProp2 = (RelProp) AsgQueryUtil.element(asgQueryWithRelProps, 5).get().geteBase();
        rProp2.setpType("dateSinceTheBigBang"); //this is a date field - Input is long type - epoch time
        rProp2.getCon().setExpr(5555L); //Epoch time as Long
        assertThat(rProp2.getCon().getExpr(), instanceOf(Long.class));



        RelProp rProp3 = (RelProp) AsgQueryUtil.element(asgQueryWithRelPropsOriginal, 5).get().geteBase();
        rProp3.setpType("dateSinceTheBigBang"); //this is a date field - Input is long type - epoch time
        rProp3.getCon().setExpr(5555L); //Epoch time as Long
        assertThat(rProp3.getCon().getExpr(), instanceOf(Long.class));

        RelProp rProp4 = (RelProp) AsgQueryUtil.element(asgQueryWithRelPropsOriginal, 4).get().geteBase();
        rProp4.setpType("dateSinceTheBigBang"); //this is a date field - Input is long type - epoch time
        rProp4.getCon().setExpr(123456L); //Epoch time as Long
        assertThat(rProp4.getCon().getExpr(), instanceOf(Long.class));
        //endregion

        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(ont);
        ConstraintTypeTransformationAsgStrategy asgConstraintTypeTransformationStrategy = new ConstraintTypeTransformationAsgStrategy();

        //Applying the Strategy on the RelProp #1 with the Epoch time
        asgConstraintTypeTransformationStrategy.apply(asgQueryWithRelProps, asgStrategyContext);
        Object expr1 = ((RelProp) AsgQueryUtil.element(asgQueryWithRelProps, 4).get().geteBase()).getCon().getExpr();
        Object expr2 = ((RelProp) AsgQueryUtil.element(asgQueryWithRelProps, 5).get().geteBase()).getCon().getExpr();
        assertThat(expr1, instanceOf(Date.class));
        assertThat(expr2, instanceOf(Date.class));

        //Appling First the Properties Grouping Startegy and then applying the constraint transformation strategy
        //We want to be sure that the order of strategies is not affecting the final result
        RelPropGroupingAsgStrategy relPropGroupingAsgStrategy = new RelPropGroupingAsgStrategy();
        relPropGroupingAsgStrategy.apply(asgQueryWithRelPropsOriginal, new AsgStrategyContext(ont));

        expr1 = ((RelPropGroup) AsgQueryUtil.element(asgQueryWithRelPropsOriginal, 4).get().geteBase()).getProps().get(0).getCon().getExpr();
        expr2 = ((RelPropGroup) AsgQueryUtil.element(asgQueryWithRelPropsOriginal, 4).get().geteBase()).getProps().get(1).getCon().getExpr();

        assertThat(expr1, instanceOf(Long.class));
        assertThat(expr2, instanceOf(Long.class));

        //Checking the RelProps grouping mechanism
        AsgEBase<EBase> newRelPropGroupAsgEbase = AsgQueryUtil.element(asgQueryWithRelPropsOriginal, 4).get();
        assertNotNull(newRelPropGroupAsgEbase);

        //Applying again the Constraint Transformation Strategy
        asgConstraintTypeTransformationStrategy.apply(asgQueryWithRelPropsOriginal, asgStrategyContext);

        expr1 = ((RelPropGroup) AsgQueryUtil.element(asgQueryWithRelPropsOriginal, 4).get().geteBase()).getProps().get(0).getCon().getExpr();
        expr2 = ((RelPropGroup) AsgQueryUtil.element(asgQueryWithRelPropsOriginal, 4).get().geteBase()).getProps().get(1).getCon().getExpr();

        assertThat(expr1, instanceOf(Date.class));
        assertThat(expr2, instanceOf(Date.class));

    }
    //endregion


    //region Fields
    private Ontology.Accessor ont;
    //endregion

}