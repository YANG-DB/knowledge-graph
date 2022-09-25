package org.opensearch.graph.asg.strategy.constraint;

import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.junit.Assert;
import org.junit.Test;

import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.ePropGroup;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.quant1;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.typed;
import static org.opensearch.graph.model.query.quant.QuantType.all;

public class LikeToEqTransformationAsgStrategyTest {

    private AsgQuery Q1() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "abc"))))
                .build();
        return asgQuery;
    }

    private AsgQuery Q2() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "*abc"))))
                .build();
        return asgQuery;
    }


    @Test
    public void testTransformLike(){
        AsgQuery query = Q1();
        LikeToEqTransformationAsgStrategy strategy = new LikeToEqTransformationAsgStrategy();
        strategy.apply(query, null);
        EPropGroup actualGroup = (EPropGroup) query.getStart().getNext().get(0).getNext().get(0).getNext().get(0).geteBase();
        Assert.assertEquals(1, actualGroup.getProps().size());
        Assert.assertTrue(actualGroup.getProps().get(0).getCon().equals(Constraint.of(ConstraintOp.eq, "abc")));
    }

    @Test
    public void testNotTransformedLike(){
        AsgQuery query = Q2();
        LikeToEqTransformationAsgStrategy strategy = new LikeToEqTransformationAsgStrategy();
        strategy.apply(query, null);
        EPropGroup actualGroup = (EPropGroup) query.getStart().getNext().get(0).getNext().get(0).getNext().get(0).geteBase();
        Assert.assertEquals(1, actualGroup.getProps().size());
        Assert.assertTrue(actualGroup.getProps().get(0).getCon().equals(Constraint.of(ConstraintOp.like, "*abc")));
    }
}
