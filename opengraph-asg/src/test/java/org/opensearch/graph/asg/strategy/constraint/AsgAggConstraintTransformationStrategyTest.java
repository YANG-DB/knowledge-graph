package org.opensearch.graph.asg.strategy.constraint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Supplier;
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
import org.opensearch.graph.model.query.aggregation.Agg;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.RelProp;
import org.opensearch.graph.model.query.properties.RelPropGroup;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.opensearch.graph.model.query.properties.constraint.CountConstraintOp;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Created by benishue on 09-May-17.
 */
public class AsgAggConstraintTransformationStrategyTest {
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

        Agg agg = new Agg(2,"count(Own)",Constraint.of(CountConstraintOp.ge,5),3);
        elements.add(agg);
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
        rel.seteNum(3);
        rel.setrType("Own");
        rel.setDir(Rel.Direction.R);
        rel.setNext(4);
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
        eTypedB.seteNum(4);
        eTypedB.seteTag("B");
        eTypedB.seteType("Dragon");
        eTypedB.setNext(5);
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
        eProp.seteNum(5);
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
    public void asgConstraintTransformationStrategyVerifyAggTest() throws Exception {
        AsgQuery asgQueryWithEProps = Q1();

        //Setting The EProp expression as a date represented by Long value
        Agg agg = (Agg) AsgQueryUtil.element(asgQueryWithEProps, 2).get().geteBase();
        assertThat(agg.getCon().getExpr(), instanceOf(Integer.class));

      }


    //endregion

    //endregion

    //region Fields
    private Ontology.Accessor ont;
    //endregion

}