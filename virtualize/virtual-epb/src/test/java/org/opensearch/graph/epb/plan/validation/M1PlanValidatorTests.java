package org.opensearch.graph.epb.plan.validation;

import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.dispatcher.epb.PlanValidator;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
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

import static org.opensearch.graph.epb.utils.PlanMockUtils.PlanMockBuilder.mock;
import static org.opensearch.graph.model.OntologyTestUtils.*;
import static org.opensearch.graph.model.OntologyTestUtils.Gender.MALE;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.*;
import static org.opensearch.graph.model.query.Rel.Direction.R;
import static org.opensearch.graph.model.query.properties.RelProp.of;
import static org.opensearch.graph.model.query.quant.QuantType.all;

/**
 * Created by Roman on 04/05/2017.
 */
public class M1PlanValidatorTests {
    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, OntologyTestUtils.PERSON.type, "A").next(ePropGroup(101)))
                .next(rel(2, OWN.getrType(), R))
                .next(typed(3, OntologyTestUtils.DRAGON.type, "B")).build();
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

    //region Valid Plan Tests
    @Test
    public void testValidPlan_entity1_rel2() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 101).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlan_entity3_rel2() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(reverseRelation(AsgQueryUtil.<Rel>element(asgQuery, 2).get()))
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlan_entity3_rel2_entity1() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(reverseRelation(AsgQueryUtil.<Rel>element(asgQuery, 2).get())),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlan_entity3_filter9() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 9).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3_rel5() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlan_entity1_rel2_filter10_entity3_filter9_rel5() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(asgQuery, 10).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 9).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlan_entity3_rel2_entity1_goto3_rel5() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(reverseRelation(AsgQueryUtil.<Rel>element(asgQuery, 2).get())),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new GoToEntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlan_entity3_filter9_rel2_filter10_entity1_goto3_rel5() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 9).get()),
                new RelationOp(reverseRelation(AsgQueryUtil.<Rel>element(asgQuery, 2).get())),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(asgQuery, 10).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new GoToEntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlan_entity3_rel2_entity1_goto3_rel5_entity6() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(reverseRelation(AsgQueryUtil.<Rel>element(asgQuery, 2).get())),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new GoToEntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 6).get())
        );
        boolean planValid = validator.isPlanValid(plan, asgQuery).valid();
        Assert.assertTrue(planValid);
    }

    @Test
    public void testValidPlan_entity3_filter9_rel2_filter10_entity1_goto3_rel5_entity6() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 9).get()),
                new RelationOp(reverseRelation(AsgQueryUtil.<Rel>element(asgQuery, 2).get())),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(asgQuery, 10).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new GoToEntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 6).get())
        );

        boolean planValid = validator.isPlanValid(plan, asgQuery).valid();
        Assert.assertTrue(planValid);
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3_rel5_entity6() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 6).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlan_entity1_rel2_filter10_entity3_filter9_rel5_entity6() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(asgQuery, 10).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 9).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 6).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlan_entity6_rel5_entity3_rel2_entity1() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 6).get()),
                new RelationOp(reverseRelation(AsgQueryUtil.<Rel>element(asgQuery, 5).get())),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(reverseRelation(AsgQueryUtil.<Rel>element(asgQuery, 2).get())),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlan_entity6_rel5_entity3_filter9_rel2_filter10_entity1() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 6).get()),
                new RelationOp(reverseRelation(AsgQueryUtil.<Rel>element(asgQuery, 5).get())),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 9).get()),
                new RelationOp(reverseRelation(AsgQueryUtil.<Rel>element(asgQuery, 2).get())),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(asgQuery, 10).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3_rel7() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 7).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlan_entity1_rel2_filter10_entity3_filter9_rel7() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(asgQuery, 10).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 9).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 7).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3_rel7_entity8() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 7).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 8).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlan_entity1_rel2_filter10_entity3_filter9_rel7_filter11_entity8() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(asgQuery, 10).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 9).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 7).get()),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(asgQuery, 11).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 8).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlanEntityOp_1_RelationOp_2_RelationFilterOp_10_EntityOp_3_EntityFilterOp_9_RelationOp_7_RelationFilterOp_11_EntityOp_8_GoToEntityOp_3_RelationOp_5_EntityOp_6() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).entityFilter(9).rel(7).relFilter(11).entity(8).goTo(3).rel(5).entity(6).plan();
        boolean planValid = validator.isPlanValid(plan, asgQuery).valid();
        Assert.assertTrue(planValid);
    }

    @Test
    public void testValidPlanEntityOp_3_EntityFilterOp_9_RelationOp_7_RelationFilterOp_11_EntityOp_8_GoToEntityOp_3_RelationOp_5_EntityOp_6_GoToEntityOp_3_RelationOp_2_RelationFilterOp_10_EntityOp_1() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = mock(asgQuery).entity(3).entityFilter(9).rel(7).relFilter(11).entity(8).goTo(3).rel(5).entity(6).goTo(3).rel(2, Rel.Direction.L).relFilter(10).entity(1).plan();
        boolean planValid = validator.isPlanValid(plan, asgQuery).valid();
        Assert.assertTrue(planValid);
    }

    @Test
    public void testValidPlan_entity8_rel7_entity3_rel2_entity1() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 8).get()),
                new RelationOp(reverseRelation(AsgQueryUtil.<Rel>element(asgQuery, 7).get())),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(reverseRelation(AsgQueryUtil.<Rel>element(asgQuery, 2).get())),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlan_entity8_rel7_filter11_entity3_filter9_rel2_filter10_entity1() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 8).get()),
                new RelationOp(reverseRelation(AsgQueryUtil.<Rel>element(asgQuery, 7).get())),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(asgQuery, 11).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 9).get()),
                new RelationOp(reverseRelation(AsgQueryUtil.<Rel>element(asgQuery, 2).get())),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(asgQuery, 10).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }
    //endregion

    //region Invalid Plan Tests
    @Test
    public void testIValidPlan_entity3_NotAdjacen() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 101).get()));

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testInvalidPlan_entity1_entity3() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testInvalidPlan_entity3_entity1() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testInvalidPlan_rel2() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testInvalidPlan_entity1_rel2_entity6() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 6).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testInvalidPlan_entity6_rel2_entity1() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 6).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testInvalidPlan_entity1_filter9() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 9).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testInvalidPlan_entity1_filter10() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(asgQuery, 10).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testInvalidPlan_entity1_rel2_filter9() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 9).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testInvalidPlan_entity1_filter9_filter10() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(asgQuery, 9).get()),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(asgQuery, 10).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testInvalidPlan_entity1_rel2_rel2() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testInvalidPlan_entity1_rel2_entity3_rel2() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testInvalidPlan_entity1_rel2_entity3_rel5_rel2() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testInvalidPlan_entity1_rel2_entity3_rel5_rel5() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testInvalidPlan_entity6_rel5_entity3_rel5() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 6).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testInvalidPlan_entity6_rel5_entity3_rel2_rel5() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 6).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }

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
    public void testInvalidPlan_entity3_rel2() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testInvalidPlan_entity1_rel2() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(reverseRelation(AsgQueryUtil.<Rel>element(asgQuery, 2).get()))
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }
    //endregion

    //region Private Methods
    protected AsgEBase<Rel> reverseRelation(AsgEBase<Rel> relAsgEBase) {
        Rel reversedRel = new Rel();
        reversedRel.seteNum(relAsgEBase.geteNum());
        reversedRel.setrType(relAsgEBase.geteBase().getrType());
        reversedRel.setDir(relAsgEBase.geteBase().getDir() == Rel.Direction.L ? Rel.Direction.R : Rel.Direction.L);

        return AsgEBase.Builder.<Rel>get().withEBase(reversedRel).build();
    }
    //endregion

    //region Fields
    protected PlanValidator<Plan, AsgQuery> validator = new M1PlanValidator();
    //endregion
}
