package org.opensearch.graph.epb.plan.validation.opValidator;

import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.dispatcher.epb.PlanValidator;
import org.opensearch.graph.epb.plan.validation.ChainedPlanValidator;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.entity.GoToEntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationFilterOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.RelPropGroup;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

import static org.opensearch.graph.model.OntologyTestUtils.*;
import static org.opensearch.graph.model.OntologyTestUtils.END_DATE;
import static org.opensearch.graph.model.OntologyTestUtils.Gender.MALE;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.concrete;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.*;
import static org.opensearch.graph.model.query.Rel.Direction.R;
import static org.opensearch.graph.model.query.properties.RelProp.of;
import static org.opensearch.graph.model.query.quant.QuantType.all;

/**
 * Created by Roman on 30/04/2017.
 */
public class RedundantGoToEntityOpValidatorTests {
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

    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, OntologyTestUtils.PERSON.type,"A"))
                .next(rel(2,OWN.getrType(),R))
                .next(typed(3, OntologyTestUtils.DRAGON.type,"B")).build();
    }

    //region Valid Plan Tests
    @Test
    public void testValidPlan_entity1_goto1() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new GoToEntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3_goto1() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new GoToEntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3_goto3() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new GoToEntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3_rel5_entity6_goto1() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 6).get()),
                new GoToEntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3_rel5_entity6_goto3() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 6).get()),
                new GoToEntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3_rel5_entity6_goto6() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 6).get()),
                new GoToEntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 6).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlan_entity1_rel2_filter10_entity3_filter9_rel5_entity6_goto3() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(asgQuery, 10).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 9).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 6).get()),
                new GoToEntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlanWithJoin(){
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityJoinOp(new Plan(new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get())),
                        new Plan(new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 9).get()),
                                new RelationOp( AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(asgQuery, 10).get()),
                                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()))),
                new GoToEntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 6).get())

        );
        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }
    //endregion

    //region Invalid Plan Tests
    @Test
    public void testInvalidPlan_entity1_goto3() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new GoToEntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testInvalidPlan_entity1_rel2_goto3() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new GoToEntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testInvalidPlanWithJoin(){
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityJoinOp(new Plan(new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get())),
                        new Plan(new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 9).get()),
                                new RelationOp( AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(asgQuery, 10).get()),
                                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()))),
                new GoToEntityOp(AsgQueryUtil.element$(asgQuery, 6)));
        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }
    //endregion

    //region Fields
    private PlanValidator<Plan, AsgQuery> validator = new ChainedPlanValidator(
            new CompositePlanOpValidator(
                    CompositePlanOpValidator.Mode.all,
                    new RedundantGoToEntityOpValidator()));

    //endregion
}
