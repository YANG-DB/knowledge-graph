package org.opensearch.graph.epb.plan.extenders;

import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanAssert;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.ScoreEProp;
import org.opensearch.graph.model.query.properties.ScoreEPropGroup;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import javaslang.collection.Stream;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.opensearch.graph.model.OntologyTestUtils.*;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.query.Rel.Direction.R;
import static org.opensearch.graph.model.query.properties.RelProp.of;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by Roman on 23/04/2017.
 */
public class InitialPlanBoostExtenderStrategyTest {


    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, PERSON.type,"A"))
                .next(new AsgEBase<>(new ScoreEPropGroup(2,100, EProp.of(3, "pTyple", Constraint.of(eq, "1234")))))
                .next(rel(4,OWN.getrType(),R))
                .next(typed(5, DRAGON.type,"B"))
                .next(new AsgEBase<>(new ScoreEPropGroup(6,100, EProp.of(7, "pTyple", Constraint.of(eq, "1234"))))).build();
    }


    public static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, PERSON.type,"A"))
                .next(ePropGroup(2,new ScoreEProp(3, "pTyple", Constraint.of(eq, "1234"), 100)))
                .next(rel(4,OWN.getrType(),R))
                .next(typed(5, DRAGON.type,"B")).build();

    }

    @Test
    public void test_simpleQuery1_seedPlan() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan expectedPlan1 = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 2)));

        List<Plan> extendedPlans = Stream.ofAll(new InitialPlanBoostExtensionStrategy().extendPlan(Optional.empty(), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(),1);
        Plan actualPlan1 = extendedPlans.get(0);
        PlanAssert.assertEquals(expectedPlan1, actualPlan1);
    }

    @Test
    public void test_simpleQuery2_seedPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan expectedPlan1 = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 2)));

        List<Plan> extendedPlans = Stream.ofAll(new InitialPlanBoostExtensionStrategy().extendPlan(Optional.empty(), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(),1);
        Plan actualPlan1 = extendedPlans.get(0);
        PlanAssert.assertEquals(expectedPlan1, actualPlan1);
    }

}
