package org.opensearch.graph.epb.plan.extenders;

import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.entity.EntityNoOp;
import org.opensearch.graph.model.execution.plan.entity.GoToEntityOp;
import org.opensearch.graph.model.execution.plan.PlanAssert;
import org.opensearch.graph.model.execution.plan.composite.OptionalOp;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
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
 * Created by Roman on 11/25/2017.
 */
public class OptionalInitialExtensionStrategyTest {
    //region Tests
    @Test
    public void test_query1_initialOptionalDownstream() {
        AsgQuery asgQuery = query1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 9)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 10)));

        Plan expectedPlan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new OptionalOp(AsgQueryUtil.element$(asgQuery, 8),
                        new EntityNoOp(AsgQueryUtil.element$(asgQuery, 3)),
                        new RelationOp(AsgQueryUtil.element$(asgQuery, 9)),
                        new EntityOp(AsgQueryUtil.element$(asgQuery, 10))));

        List<Plan> extendedPlans = Stream.ofAll(new OptionalInitialExtensionStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertEquals(1, extendedPlans.size());
        PlanAssert.assertEquals(expectedPlan, extendedPlans.get(0));
    }

    @Test
    public void test_query1_initialOptionalUpstream() {
        AsgQuery asgQuery = query1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 10)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 9)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)));

        Plan expectedPlan = new Plan(
                new OptionalOp(AsgQueryUtil.element$(asgQuery, 8),
                        new EntityOp(AsgQueryUtil.element$(asgQuery, 10)),
                        new RelationOp(AsgQueryUtil.element$(asgQuery, 9)),
                        new EntityOp(AsgQueryUtil.element$(asgQuery, 3))));

        List<Plan> extendedPlans = Stream.ofAll(new OptionalInitialExtensionStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertEquals(1, extendedPlans.size());
        PlanAssert.assertEquals(expectedPlan, extendedPlans.get(0));
    }

    @Test
    public void test_query1_initialOptionalDownstreamWithGoto() {
        AsgQuery asgQuery = query1("name", "ont");
        Plan plan = new Plan(
                new GoToEntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 9)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 10)));

        Plan expectedPlan = new Plan(
                new GoToEntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new OptionalOp(AsgQueryUtil.element$(asgQuery, 8),
                        new EntityNoOp(AsgQueryUtil.element$(asgQuery, 3)),
                        new RelationOp(AsgQueryUtil.element$(asgQuery, 9)),
                        new EntityOp(AsgQueryUtil.element$(asgQuery, 10))));

        List<Plan> extendedPlans = Stream.ofAll(new OptionalInitialExtensionStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertEquals(1, extendedPlans.size());
        PlanAssert.assertEquals(expectedPlan, extendedPlans.get(0));
    }

    @Test
    public void test_query1_initialOptionalUpstreamWithGoto() {
        AsgQuery asgQuery = query1("name", "ont");
        Plan plan = new Plan(
                new GoToEntityOp(AsgQueryUtil.element$(asgQuery, 10)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 9)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)));

        Plan expectedPlan = new Plan(
                new OptionalOp(AsgQueryUtil.element$(asgQuery, 8),
                        new GoToEntityOp(AsgQueryUtil.element$(asgQuery, 10)),
                        new RelationOp(AsgQueryUtil.element$(asgQuery, 9)),
                        new EntityOp(AsgQueryUtil.element$(asgQuery, 3))));

        List<Plan> extendedPlans = Stream.ofAll(new OptionalInitialExtensionStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertEquals(1, extendedPlans.size());
        PlanAssert.assertEquals(expectedPlan, extendedPlans.get(0));
    }

    @Test
    public void test_query1_noOptional() {
        AsgQuery asgQuery = query1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)));

        Plan expectedPlan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)));

        List<Plan> extendedPlans = Stream.ofAll(new OptionalInitialExtensionStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertEquals(1, extendedPlans.size());
        PlanAssert.assertEquals(expectedPlan, extendedPlans.get(0));
    }
    //endregion

    //region Queries
    private static AsgQuery query1(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, "entity1", "A"))
                .next(rel(2, "rel1", R).below(relProp(2, RelProp.of(2, "2", of(eq, "value2")))))
                .next(typed(3, "entity2", "B"))
                .next(quant1(4, all))
                .in(ePropGroup(5, EProp.of(5, "prop1", of(eq, "value1")), EProp.of(5, "prop2", of(gt, "value3"))),
                    rel(6, "rel2", R).next(typed(7, "entity3", "C")),
                    optional(8).next(rel(9, "rel3", R).next(typed(10, "entity4", "D"))),
                    optional(11).next(rel(12, "rel4", R).next(typed(13, "entity4", "E")
                            .next(optional(14).next(rel(15, "rel4", R).next(typed(16, "entity4", "F")))))))
                .build();
    }
    //endregion
}
