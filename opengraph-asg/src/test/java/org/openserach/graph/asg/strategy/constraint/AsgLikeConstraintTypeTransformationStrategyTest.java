package org.openserach.graph.asg.strategy.constraint;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;

import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.query.quant.QuantType.all;
import static org.junit.Assert.*;

/**
 * Created by benishue on 09-May-17.
 */
public class AsgLikeConstraintTypeTransformationStrategyTest {
    //region Setup
    @Before
    public void setUp() throws Exception {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("Dragons_Ontology.json");
        StringWriter writer = new StringWriter();
        IOUtils.copy(stream, writer);
        ont = new Ontology.Accessor(new ObjectMapper().readValue(writer.toString(), Ontology.class));

    }
    //endregion


    //region Test Methods
    @Test
    public void asgConstraintTransformationStrategyEPropsLongToDateTest() throws Exception {
        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(ont);
        AsgQuery asgQueryWithEProps = Q1();
        RedundantLikeConstraintAsgStrategy asgConstraintTypeTransformationStrategy = new RedundantLikeConstraintAsgStrategy();

        //Applying the Strategy on the Eprop with the Epoch time
        asgConstraintTypeTransformationStrategy.apply(asgQueryWithEProps, asgStrategyContext);
        assertTrue(((EPropGroup) AsgQueryUtil.element(asgQueryWithEProps, 3).get().geteBase()).getProps().isEmpty());

        asgStrategyContext = new AsgStrategyContext(ont);
        asgQueryWithEProps = Q2();
        //Applying the Strategy on the Eprop with the Epoch time
        asgConstraintTypeTransformationStrategy.apply(asgQueryWithEProps, asgStrategyContext);
        assertEquals(((EPropGroup) AsgQueryUtil.element(asgQueryWithEProps, 3).get().geteBase()).getProps().get(0).getCon().getExpr().toString(), "a");

        asgStrategyContext = new AsgStrategyContext(ont);
        asgQueryWithEProps = Q3();
        //Applying the Strategy on the Eprop with the Epoch time
        asgConstraintTypeTransformationStrategy.apply(asgQueryWithEProps, asgStrategyContext);
        assertFalse(((EPropGroup) AsgQueryUtil.element(asgQueryWithEProps, 3).get().geteBase()).getProps().isEmpty());

        asgStrategyContext = new AsgStrategyContext(ont);
        asgQueryWithEProps = Q4();
        //Applying the Strategy on the Eprop with the Epoch time
        asgConstraintTypeTransformationStrategy.apply(asgQueryWithEProps, asgStrategyContext);
        assertEquals(((EPropGroup) AsgQueryUtil.element(asgQueryWithEProps, 3).get().geteBase()).getProps().get(0).getCon().getExpr().toString(), "b*");

        asgStrategyContext = new AsgStrategyContext(ont);
        asgQueryWithEProps = Q5();
        //Applying the Strategy on the Eprop with the Epoch time
        asgConstraintTypeTransformationStrategy.apply(asgQueryWithEProps, asgStrategyContext);
        assertTrue(((EPropGroup) AsgQueryUtil.element(asgQueryWithEProps, 3).get().geteBase()).getProps().isEmpty());

    }

    private AsgQuery Q1() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "*"))))
                .build();
        return asgQuery;
    }

    private AsgQuery Q2() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "a")), EProp.of(3, "name", Constraint.of(ConstraintOp.like, "*"))))
                .build();
        return asgQuery;
    }

    private AsgQuery Q3() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "a")), EProp.of(3, "name", Constraint.of(ConstraintOp.like, "b*"))))
                .build();
        return asgQuery;
    }

    private AsgQuery Q4() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.likeAny, Arrays.asList("a", "*", "b"))), EProp.of(3, "name", Constraint.of(ConstraintOp.like, "b*"))))
                .build();
        return asgQuery;
    }

    private AsgQuery Q5() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.likeAny, Arrays.asList("a", "*", "b"))), EProp.of(3, "name", Constraint.of(ConstraintOp.like, "***"))))
                .build();
        return asgQuery;
    }


    //endregion

    //region Fields
    private Ontology.Accessor ont;
    //endregion

}