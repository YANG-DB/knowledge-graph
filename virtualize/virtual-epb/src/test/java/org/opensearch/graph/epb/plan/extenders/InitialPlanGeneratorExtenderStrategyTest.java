package org.opensearch.graph.epb.plan.extenders;

import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.PlanAssert;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.EProp;
import javaslang.collection.Stream;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.opensearch.graph.model.OntologyTestUtils.*;
import static org.opensearch.graph.model.OntologyTestUtils.Gender.MALE;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.*;
import static org.opensearch.graph.model.query.Rel.Direction.R;
import static org.opensearch.graph.model.query.properties.RelProp.of;
import static org.opensearch.graph.model.query.quant.QuantType.all;
import static org.junit.Assert.assertEquals;

/**
 * Created by Roman on 23/04/2017.
 */
public class InitialPlanGeneratorExtenderStrategyTest {

    /**
     * +----+       +---------+               +---------+
     * |S(0)| +--+  |eTyped(1)| +--rel(2)+--> |eTyped(3)|
     * +----+       +---------+               +---------+
     *
     * @param queryName
     * @param ontologyName
     * @return
     */
    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, OntologyTestUtils.PERSON.type,"A"))
                .next(rel(2,OWN.getrType(),R))
                .next(typed(3, OntologyTestUtils.DRAGON.type,"B")).build();
    }

    public static AsgQuery simpleQuery3(String queryName, String ontologyName) {
        long time = System.currentTimeMillis();
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, PERSON.type))
                .next(rel(2, OWN.getrType(), R).below(relProp(10, of(10, START_DATE.type, Constraint.of(eq, new Date())))))
                .next(typed(3, DRAGON.type))
                .next(quant1(4, all))
                .in(ePropGroup(9, EProp.of(9, NAME.type, Constraint.of(eq, "Moshe")), EProp.of(9, GENDER.type, Constraint.of(gt, MALE)))
                        , rel(5, FIRE.getrType(), R)
                                .next(unTyped(6)
                                        .next(rel(12, REGISTERED.getrType(), R)
                                                .next(typed(13, KINGDOM.type))
                                        )
                                )
                        , rel(7, FREEZE.getrType(), R)
                                .below(relProp(11, of(11, START_DATE.type,
                                        Constraint.of(ge, new Date(time - 1000 * 60))),
                                        of(11, END_DATE.type, Constraint.of(le, new Date(time + 1000 * 60)))))
                                .next(concrete(8, "Beltazar", DRAGON.type, "Beltazar", "D")
                                        .next(rel(14, ORIGIN.getrType(), R)
                                                .next(typed(15, KINGDOM.type))
                                        )
                                )
                ).build();
    }

    public static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        long time = System.currentTimeMillis();
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, PERSON.type))
                .next(rel(2, OWN.getrType(), R).below(relProp(10, of(10, START_DATE.type, Constraint.of(eq, new Date())))))
                .next(typed(3, DRAGON.type))
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
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan expectedPlan1 = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)));
        Plan expectedPlan2 = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 3)));

        List<Plan> extendedPlans = Stream.ofAll(new InitialPlanGeneratorExtensionStrategy().extendPlan(Optional.empty(), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(),2);
        Plan actualPlan1 = extendedPlans.get(0);
        PlanAssert.assertEquals(expectedPlan1, actualPlan1);
        Plan actualPlan2 = extendedPlans.get(1);
        PlanAssert.assertEquals(expectedPlan2, actualPlan2);
    }

    @Test
    public void test_simpleQuery2_seedPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        List<Plan> extendedPlans = Stream.ofAll(new InitialPlanGeneratorExtensionStrategy().extendPlan(Optional.empty(), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(),4);

        assertEquals(extendedPlans.get(0).getOps().size(), 1);
        assertEquals(extendedPlans.get(1).getOps().size(), 2);
        assertEquals(extendedPlans.get(2).getOps().size(), 1);
        assertEquals(extendedPlans.get(3).getOps().size(), 1);
    }

    @Test
    public void test_simpleQuery3_seedPlan() {
        AsgQuery asgQuery = simpleQuery3("name", "ont");
        List<Plan> extendedPlans = Stream.ofAll(new InitialPlanGeneratorExtensionStrategy().extendPlan(Optional.empty(), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(),6);

        assertEquals(extendedPlans.get(0).getOps().size(), 1);
        assertEquals(extendedPlans.get(1).getOps().size(), 2);
        assertEquals(extendedPlans.get(2).getOps().size(), 1);
        assertEquals(extendedPlans.get(3).getOps().size(), 1);
        assertEquals(extendedPlans.get(4).getOps().size(), 1);
        assertEquals(extendedPlans.get(5).getOps().size(), 1);
    }
}
