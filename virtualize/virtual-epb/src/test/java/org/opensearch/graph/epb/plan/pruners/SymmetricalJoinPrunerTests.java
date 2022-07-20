package org.opensearch.graph.epb.plan.pruners;

import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static org.opensearch.graph.model.OntologyTestUtils.OWN;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.rel;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.typed;
import static org.opensearch.graph.model.query.Rel.Direction.R;

public class SymmetricalJoinPrunerTests {

    public static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, OntologyTestUtils.PERSON.type,"A"))
                .next(rel(2,OWN.getrType(),R))
                .next(typed(3, OntologyTestUtils.DRAGON.type,"B"))
                .next(rel(4,OWN.getrType(),R))
                .next(typed(5, OntologyTestUtils.DRAGON.type,"C")).build();
    }

    @Test
    public void noPruningTest(){
        AsgQuery query = simpleQuery2("1", "2");

        Plan plan1 = new Plan(new EntityJoinOp(
                new Plan(new EntityOp(AsgQueryUtil.element$(query, 1))),
                new Plan(new EntityOp(AsgQueryUtil.element$(query, 3)),new RelationOp(AsgQueryUtil.element$(query, 2)),new EntityOp(AsgQueryUtil.element$(query, 1)))
        ));

        Plan plan2 = new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)));
        Plan plan3 = new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)),new RelationOp(AsgQueryUtil.element$(query, 2)),new EntityOp(AsgQueryUtil.element$(query, 3)));
        SymmetricalJoinPruner pruner = new SymmetricalJoinPruner();
        Iterable<PlanWithCost<Plan, PlanDetailedCost>> planWithCosts = pruner.prunePlans(Arrays.asList(new PlanWithCost<Plan, PlanDetailedCost>(plan1, null), new PlanWithCost<>(plan2, null), new PlanWithCost<>(plan3, null)));
        Assert.assertEquals(3, Stream.ofAll(planWithCosts).length());
    }

    @Test
    public void pruningTest(){
        AsgQuery query = simpleQuery2("1", "2");

        Plan plan1 = new Plan(new EntityJoinOp(
                new Plan(new EntityOp(AsgQueryUtil.element$(query, 1))),
                new Plan(new EntityOp(AsgQueryUtil.element$(query, 3)),new RelationOp(AsgQueryUtil.element$(query, 2)),new EntityOp(AsgQueryUtil.element$(query, 1))),
                true

        ));

        Plan plan2 = new Plan(new EntityJoinOp(
                new Plan(new EntityOp(AsgQueryUtil.element$(query, 3)),new RelationOp(AsgQueryUtil.element$(query, 2)),new EntityOp(AsgQueryUtil.element$(query, 1))),
                new Plan(new EntityOp(AsgQueryUtil.element$(query, 1))),
                true
        ));


        SymmetricalJoinPruner pruner = new SymmetricalJoinPruner();
        Iterable<PlanWithCost<Plan, PlanDetailedCost>> planWithCosts = pruner.prunePlans(Arrays.asList(new PlanWithCost<Plan, PlanDetailedCost>(plan1, null), new PlanWithCost<>(plan2, null)));
        Assert.assertEquals(1, Stream.ofAll(planWithCosts).length());
    }
}
