package org.opensearch.graph.epb.plan.extenders.M1;

import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.dispatcher.epb.PlanExtensionStrategy;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.*;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationFilterOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.RelProp;
import javaslang.collection.Stream;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.opensearch.graph.epb.utils.PlanMockUtils.PlanMockBuilder.mock;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.query.properties.constraint.Constraint.of;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.eq;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.gt;
import static org.opensearch.graph.model.query.Rel.Direction.L;
import static org.opensearch.graph.model.query.Rel.Direction.R;
import static org.opensearch.graph.model.query.quant.QuantType.all;
import static org.junit.Assert.assertEquals;

public class M1NonRedundantPlanExtensionStrategyTest {

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
                                .next(concrete( 8, "concrete1", "3", "Concrete1", "D")
                                        .next(rel(14, "1", R)
                                                .next(typed(15, "1", "F"))
                                        )
                                )
                )
                .build();
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

    @Test
    public void test_simpleQuery0seedPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");

        PlanExtensionStrategy<Plan, AsgQuery> strategy = new M1NonRedundantPlanExtensionStrategy();
        List<Plan> extendedPlans = Stream.ofAll(strategy.extendPlan(Optional.empty(), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 4);
        PlanAssert.assertEquals(mock(asgQuery).entity(1).plan(), extendedPlans.get(0));
        PlanAssert.assertEquals(mock(asgQuery).entity(3).entityFilter(9).plan(), extendedPlans.get(1));
        PlanAssert.assertEquals(mock(asgQuery).entity(6).plan(), extendedPlans.get(2));
        PlanAssert.assertEquals(mock(asgQuery).entity(8).plan(), extendedPlans.get(3));
    }

    @Test
    public void test_simpleQuery1seedPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");

        Plan startPlan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)));

        PlanExtensionStrategy<Plan, AsgQuery> strategy = new M1NonRedundantPlanExtensionStrategy();
        List<Plan> extendedPlans = Stream.ofAll(strategy.extendPlan(Optional.of(startPlan), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 1);
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).entityFilter(9).plan(), extendedPlans.get(0));
    }

    @Test
    public void test_simpleQuery2ElementsPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");

        PlanExtensionStrategy<Plan, AsgQuery> strategy = new M1NonRedundantPlanExtensionStrategy();
        List<Plan> extendedPlans = Stream.ofAll(strategy.extendPlan(
                Optional.of(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).plan()), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 4);

        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).goTo(1).rel(2).relFilter(10).entity(3).entityFilter(9).plan(), extendedPlans.get(0));
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(2, L).relFilter(10).entity(1).plan(), extendedPlans.get(1));
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(5).entity(6).plan(), extendedPlans.get(2));
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(7).relFilter(11).entity(8).plan(), extendedPlans.get(3));
    }


    /**
     *
     */
    @Test
    public void test_simpleQuery4ElementsPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 5)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 6)));

        PlanExtensionStrategy<Plan, AsgQuery> strategy = new M1NonRedundantPlanExtensionStrategy();
        List<Plan> extendedPlans = Stream.ofAll(strategy.extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 5);
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(5).entity(6).goTo(1).rel(2).relFilter(10).entity(3).entityFilter(9).plan(), extendedPlans.get(0));
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(5).entity(6).goTo(3).rel(2, L).relFilter(10).entity(1).plan(), extendedPlans.get(1));
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(5).entity(6).goTo(3).rel(5).entity(6).plan(), extendedPlans.get(2));
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(5).entity(6).goTo(3).rel(7).relFilter(11).entity(8).plan(), extendedPlans.get(3));
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(5).entity(6).rel(5, L).entity(3).entityFilter(9).plan(), extendedPlans.get(4));
    }

    @Test
    public void test_simpleQuery5ElementsPlan() {
        AsgQuery asgQuery = simpleQuery3("name", "ont");

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 5)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 6)));

        PlanExtensionStrategy<Plan, AsgQuery> strategy = new M1NonRedundantPlanExtensionStrategy();
        List<Plan> extendedPlans = Stream.ofAll(strategy.extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 6);
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(5).entity(6).goTo(1).rel(2).relFilter(10).entity(3).entityFilter(9).plan(), extendedPlans.get(0));
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(5).entity(6).goTo(3).rel(2, L).relFilter(10).entity(1).plan(), extendedPlans.get(1));
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(5).entity(6).goTo(3).rel(5).entity(6).plan(), extendedPlans.get(2));
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(5).entity(6).goTo(3).rel(7).relFilter(11).entity(8).plan(), extendedPlans.get(3));
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(5).entity(6).rel(5, L).entity(3).entityFilter(9).plan(), extendedPlans.get(4));
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(5).entity(6).rel(12).entity(13).plan(), extendedPlans.get(5));
    }
}
