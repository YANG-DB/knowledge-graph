package org.opensearch.graph.epb.plan.validation;

import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationFilterOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.validation.ValidationResult;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

import static org.opensearch.graph.model.OntologyTestUtils.*;
import static org.opensearch.graph.model.OntologyTestUtils.END_DATE;
import static org.opensearch.graph.model.OntologyTestUtils.Gender.MALE;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.*;
import static org.opensearch.graph.model.query.Rel.Direction.R;
import static org.opensearch.graph.model.query.properties.RelProp.of;
import static org.opensearch.graph.model.query.quant.QuantType.all;

public class M2PlanValidatorTests extends M1PlanValidatorTests{

    public M2PlanValidatorTests() {
        this.validator = new M2PlanValidator();
    }

    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, OntologyTestUtils.PERSON.type,"A"))
                .next(rel(2,OWN.getrType(),R))
                .next(typed(3, OntologyTestUtils.DRAGON.type,"B")).build();
    }

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
    public void testJoinInValidLeftPlanSimpleQueryPartialPlan(){
        AsgQuery query = simpleQuery1("q", "ont");
        Plan plan = new Plan(
                new EntityJoinOp(new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)))
                        , new Plan(new EntityOp(AsgQueryUtil.element$(query,3)))));
        ValidationResult planValid = this.validator.isPlanValid(plan, query);
        Assert.assertFalse(planValid.valid());

    }

    @Test
    public void testJoinInValidPlanSimpleQuery(){
        AsgQuery query = simpleQuery1("q", "ont");
        Plan plan = new Plan(
                new EntityJoinOp(new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)))
                        , new Plan(new EntityOp(AsgQueryUtil.element$(query,3)),
                        new RelationOp(reverseRelation(AsgQueryUtil.element$(query,2))),
                        new EntityOp(AsgQueryUtil.element$(query,1)))));
        ValidationResult planValid = this.validator.isPlanValid(plan, query);
        Assert.assertFalse(planValid.valid());

    }
    @Test
    public void testJoinInValidPlanSimpleQueryPartialPlan(){
        AsgQuery query = simpleQuery1("q", "ont");
        Plan plan = new Plan(
                new EntityJoinOp(new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)))
                        , new Plan(new EntityOp(AsgQueryUtil.element$(query,1)),
                        new RelationOp(AsgQueryUtil.element$(query,2)),
                        new EntityOp(AsgQueryUtil.element$(query,3)))));
        ValidationResult planValid = this.validator.isPlanValid(plan, query);
        Assert.assertFalse(planValid.valid());

    }

    @Test
    public void testJoinHierarchy(){
        AsgQuery query = simpleQuery2("q", "ont");
        Plan left1 = new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)),
                new RelationOp(AsgQueryUtil.element$(query, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(query, 10)),
                new EntityOp(AsgQueryUtil.element$(query, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 9)));
        Plan right1 = new Plan(new EntityOp(AsgQueryUtil.element$(query, 6)),
                new RelationOp(reverseRelation(AsgQueryUtil.element$(query, 5))),
                new EntityOp(AsgQueryUtil.element$(query, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 9)));

        EntityJoinOp joinOp1 = new EntityJoinOp(left1, right1, true);
        Plan right2 = new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 8)),
                new RelationOp(reverseRelation(AsgQueryUtil.element$(query, 7))),
                new RelationFilterOp(AsgQueryUtil.element$(query, 11)),
                new EntityOp(AsgQueryUtil.element$(query, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 9)));
        EntityJoinOp joinOp2 = new EntityJoinOp(new Plan(joinOp1), right2,true);


        ValidationResult validationContext = this.validator.isPlanValid(new Plan(joinOp2), query);
        Assert.assertTrue(validationContext.valid());

    }

    @Test
    public void testJoinHierarchyInvalid(){
        AsgQuery query = simpleQuery2("q", "ont");
        Plan left1 = new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)));
        Plan right1 = new Plan(new EntityOp(AsgQueryUtil.element$(query, 6)),
                new RelationOp(reverseRelation(AsgQueryUtil.element$(query, 5))),
                new EntityOp(AsgQueryUtil.element$(query, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 9)));

        EntityJoinOp joinOp1 = new EntityJoinOp(left1, right1);
        Plan right2 = new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 8)),
                new RelationOp(reverseRelation(AsgQueryUtil.element$(query, 7))),
                new RelationFilterOp(AsgQueryUtil.element$(query, 11)),
                new EntityOp(AsgQueryUtil.element$(query, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 9)));
        EntityJoinOp joinOp2 = new EntityJoinOp(new Plan(joinOp1), right2);


        ValidationResult validationContext = this.validator.isPlanValid(new Plan(joinOp2), query);
        Assert.assertFalse(validationContext.valid());

    }

    @Test
    public void testJoinHierarchyInvalidRightBranch(){
        AsgQuery query = simpleQuery2("q", "ont");
        Plan left1 = new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)),
                new RelationOp(AsgQueryUtil.element$(query, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(query, 10)),
                new EntityOp(AsgQueryUtil.element$(query, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 9)));
        Plan right1 = new Plan(new EntityOp(AsgQueryUtil.element$(query, 6)));

        EntityJoinOp joinOp1 = new EntityJoinOp(left1, right1);
        Plan right2 = new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 8)),
                new RelationOp(reverseRelation(AsgQueryUtil.element$(query, 7))),
                new RelationFilterOp(AsgQueryUtil.element$(query, 11)),
                new EntityOp(AsgQueryUtil.element$(query, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 9)));
        EntityJoinOp joinOp2 = new EntityJoinOp(new Plan(joinOp1), right2);


        ValidationResult validationContext = this.validator.isPlanValid(new Plan(joinOp2), query);
        Assert.assertFalse(validationContext.valid());

    }
}
