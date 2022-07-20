package org.opensearch.graph.epb.plan.extenders;

import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.*;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.entity.GoToEntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationFilterOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.Start;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.RelProp;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.query.properties.constraint.Constraint.of;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.eq;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.gt;
import static org.opensearch.graph.model.query.Rel.Direction.R;
import static org.opensearch.graph.model.query.quant.QuantType.all;

/**
 * Created by Roman on 23/04/2017.
 */
public class StepDescendantsAdjacentStrategyTest {
    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        Start start = new Start();
        start.seteNum(0);

        ETyped eTyped = new ETyped();
        eTyped.seteNum(1);
        eTyped.seteTag("A");
        eTyped.seteType("1");

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setDir(R);
        rel.setrType("1");

        ETyped eTyped2 = new ETyped();
        eTyped2.seteNum(3);
        eTyped2.seteTag("B");
        eTyped2.seteType("2");

        AsgEBase<Start> asgStart =
                AsgEBase.Builder.<Start>get().withEBase(start)
                        .withNext(AsgEBase.Builder.get().withEBase(eTyped)
                                .withNext(AsgEBase.Builder.get().withEBase(rel)
                                        .withNext(AsgEBase.Builder.get().withEBase(eTyped2)
                                                .build())
                                        .build())
                                .build())
                        .build();

        return AsgQuery.AsgQueryBuilder.anAsgQuery().withName(queryName).withOnt(ontologyName).withStart(asgStart).build();
    }

    public static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, "1", "A"))
                .next(rel(2, "1", R).below(relProp(10, RelProp.of(10, "2", of(eq, "value2")))))
                .next(typed(3, "2", "B"))
                .next(quant1(4, all))
                .in(ePropGroup(9, EProp.of(9, "1", of(eq, "value1")), EProp.of(9, "3", of(gt, "value3")))
                        , rel(5, "4", R)
                                .next(unTyped(6, "C"))
                        , rel(7, "5", R)
                                .below(relProp(11, RelProp.of(11, "5", of(eq, "value5")), RelProp.of(11, "4", of(eq, "value4"))))
                                .next(concrete(8, "concrete1", "3", "Concrete1", "D"))
                )
                .build();
    }

    public static AsgQuery simpleQuery3(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, "1", "A"))
                .next(rel(2, "1", R).below(relProp(10, RelProp.of(10, "2", of(eq, "value2")))))
                .next(typed(3, "2", "B"))
                .next(quant1(4, all))
                .in(ePropGroup(9, EProp.of(9, "1", of(eq, "value1")), EProp.of(9, "3", of(gt, "value3")))
                        , rel(5, "4", R)
                                .next(unTyped(6, "C")
                                        .next(rel(12, "4", R)
                                                .next(typed(13, "4", "G"))
                                        )
                                )
                        , rel(7, "5", R)
                                .below(relProp(11, RelProp.of(11, "5", of(eq, "value5")), RelProp.of(11, "4", of(eq, "value4"))))
                                .next(concrete(8, "concrete1", "3", "Concrete1", "D")
                                        .next(rel(14, "1", R)
                                                .next(typed(15, "1", "F"))
                                        )
                                )
                )
                .build();
    }

    @Test
    public void test_simpleQueryGotoSeedPlan() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan expectedPlan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)));

        Plan plan = new Plan(new GoToEntityOp(AsgQueryUtil.element$(asgQuery, 1)));
        List<Plan> extendedPlans = Stream.ofAll(new StepDescendantsAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }
    @Test
    public void test_simpleQuery1_seedPlan() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan expectedPlan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)));

        Plan plan = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 1)));
        List<Plan> extendedPlans = Stream.ofAll(new StepDescendantsAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }

    @Test
    public void test_simpleQuery1_fullPlan() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)));

        List<Plan> extendedPlans = Stream.ofAll(new StepDescendantsAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 0);
    }

    @Test
    public void test_simpleQuery2_seedPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan expectedPlan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)));

        Plan plan = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 1)));
        List<Plan> extendedPlans = Stream.ofAll(new StepDescendantsAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }

    @Test

    public void test_simpleQuery2_secondPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");

        Plan expectedPlan1 = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 5)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 6)));

        Plan expectedPlan2 = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 7)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 11)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 8)));


        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)));

        List<Plan> extendedPlans = Stream.ofAll(new StepDescendantsAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 2);
        Plan actualPlan1 = extendedPlans.get(0);
        Plan actualPlan2 = extendedPlans.get(1);

        PlanAssert.assertEquals(expectedPlan1, actualPlan1);
        PlanAssert.assertEquals(expectedPlan2, actualPlan2);
    }

    @Test
    public void test_simpleQuery2_thirdPlan() {
        AsgQuery asgQuery = simpleQuery3("name", "ont");

        Plan expectedPlan1 = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 5)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 6)));

        Plan expectedPlan2 = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 7)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 11)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 8)));

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)));

        List<Plan> extendedPlans = Stream.ofAll(new StepDescendantsAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 2);

        Plan actualPlan1 = extendedPlans.get(0);
        Plan actualPlan2 = extendedPlans.get(1);

        PlanAssert.assertEquals(expectedPlan1, actualPlan1);
        PlanAssert.assertEquals(expectedPlan2, actualPlan2);
    }

    @Test
    public void test_simpleQuery3_thirdPlan() {
        AsgQuery asgQuery = simpleQuery3("name", "ont");

        Plan expectedPlan1 = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 7)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 8)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 14)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 15)));

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 7)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 8)));

        List<Plan> extendedPlans = Stream.ofAll(new StepDescendantsAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);

        Plan actualPlan1 = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan1, actualPlan1);
    }

    @Test
    public void test_simpleQuery4_thirdPlan() {
        AsgQuery asgQuery = simpleQuery3("name", "ont");

        Plan expectedPlan1 = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 5)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 6)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 12)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 13)));

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 5)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 6)));

        List<Plan> extendedPlans = Stream.ofAll(new StepDescendantsAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);

        Plan actualPlan1 = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan1, actualPlan1);
    }
}
