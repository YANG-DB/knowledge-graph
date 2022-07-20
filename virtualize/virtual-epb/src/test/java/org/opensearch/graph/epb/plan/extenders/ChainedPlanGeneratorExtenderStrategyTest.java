package org.opensearch.graph.epb.plan.extenders;

import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.EProp;
import javaslang.collection.Stream;
import org.junit.Test;

import java.util.*;

import static org.opensearch.graph.model.OntologyTestUtils.*;
import static org.opensearch.graph.model.OntologyTestUtils.Gender.MALE;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.*;
import static org.opensearch.graph.model.query.Rel.Direction.R;
import static org.opensearch.graph.model.query.properties.RelProp.of;
import static org.opensearch.graph.model.query.quant.QuantType.all;
import static org.junit.Assert.assertEquals;

public class ChainedPlanGeneratorExtenderStrategyTest {
    public static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        long time = System.currentTimeMillis();
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, OntologyTestUtils.PERSON.type))
                .next(rel(2, OWN.getrType(), R).below(relProp(10, of(10, START_DATE.type, Constraint.of(eq, new Date())))))
                .next(typed(3, OntologyTestUtils.DRAGON.type))
                .next(quant1(4, all))
                .in(ePropGroup(9, EProp.of(9, NAME.type, Constraint.of(eq, "smith")), EProp.of(9, GENDER.type, Constraint.of(gt, MALE)))
                        , rel(5, FREEZE.getrType(), R)
                                .next(unTyped(6))
                        , rel(7, FIRE.getrType(), R)
                                .below(relProp(11, of(11, START_DATE.type,
                                        Constraint.of(ge, new Date(time - 1000 * 60))),
                                        of(11, END_DATE.type, Constraint.of(le, new Date(time + 1000 * 60)))))
                                .next(concrete(8, "smoge", DRAGON.type, "Display:smoge", "D"))
                )
                .build();
    }

    @Test
    public void test_simpleQuery1_seedPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");

        Plan startPlan = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 0)));


        ChainPlanExtensionStrategy chain = new ChainPlanExtensionStrategy<Plan, AsgQuery>(
                (plan, query) -> Arrays.asList(
                        plan.get().withOp(new EntityOp(AsgQueryUtil.element$(asgQuery, 1))),
                        plan.get().withOp(new EntityOp(AsgQueryUtil.element$(asgQuery, 3)))),
                (plan, query) -> Collections.singletonList(
                        plan.get().withOp(new RelationOp(AsgQueryUtil.element$(asgQuery, 5)))),
                (plan, query) -> Arrays.asList(
                        plan.get().withOp(new EntityOp(AsgQueryUtil.element$(asgQuery, 1))),
                        plan.get().withOp(new EntityOp(AsgQueryUtil.element$(asgQuery, 3))),
                        plan.get().withOp(new RelationOp(AsgQueryUtil.element$(asgQuery, 5)))));


        List<Plan> extendedPlans = Stream.ofAll(chain.extendPlan(Optional.of(startPlan), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 6);
        extendedPlans.forEach(p->assertEquals(p.getOps().size(),4));
    }

    @Test
    public void test_simpleQuery2_seedPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");

        Plan startPlan = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 0)));


        ChainPlanExtensionStrategy chain =  new ChainPlanExtensionStrategy<Plan, AsgQuery>(
                (plan, query) -> Arrays.asList(
                        plan.get().withOp(new EntityOp(AsgQueryUtil.element$(asgQuery, 1))),
                        plan.get().withOp(new RelationOp(AsgQueryUtil.element$(asgQuery, 3)))),
                (plan, query) -> Arrays.asList(
                        plan.get().withOp(new EntityOp(AsgQueryUtil.element$(asgQuery, 7))),
                        plan.get().withOp(new RelationOp(AsgQueryUtil.element$(asgQuery, 9)))),
                (plan, query) -> Arrays.asList(
                        plan.get().withOp(new EntityOp(AsgQueryUtil.element$(asgQuery, 1))),
                        plan.get().withOp(new EntityOp(AsgQueryUtil.element$(asgQuery, 3))),
                        plan.get().withOp(new RelationOp(AsgQueryUtil.element$(asgQuery, 5)))));


        List<Plan> extendedPlans = Stream.ofAll(chain.extendPlan(Optional.of(startPlan), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 12);
        extendedPlans.forEach(p->assertEquals(p.getOps().size(),4));
    }
}
