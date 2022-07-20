package org.opensearch.graph.epb.plan.validation.opValidator;

import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.dispatcher.epb.PlanValidator;
import org.opensearch.graph.epb.plan.validation.ChainedPlanValidator;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.junit.Assert;
import org.junit.Test;

import static org.opensearch.graph.model.OntologyTestUtils.OWN;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.rel;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.typed;
import static org.opensearch.graph.model.query.Rel.Direction.R;

/**
 * Created by Roman on 03/05/2017.
 */
public class ReverseRelationOpValidatorTests {
    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, OntologyTestUtils.PERSON.type,"A"))
                .next(rel(2,OWN.getrType(),R))
                .next(typed(3, OntologyTestUtils.DRAGON.type,"B")).build();
    }

    //region Valid Plan Tests
    @Test
    public void testValidPlan_entity1_rel2() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get())
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
    public void testValidPlan_entity3_rel2() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.reverse(AsgQueryUtil.<Rel>element(asgQuery, 2).get()))
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlan_entity3_rel2_entity1() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.reverse(AsgQueryUtil.<Rel>element(asgQuery, 2).get())),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }
    //endregion

    //region Invalid Plan
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
                new RelationOp(AsgQueryUtil.reverse(AsgQueryUtil.<Rel>element(asgQuery, 2).get()))
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }
    //endregion

    //region Fields
    private PlanValidator<Plan, AsgQuery> validator = new ChainedPlanValidator(
            new CompositePlanOpValidator(
                    CompositePlanOpValidator.Mode.all,
                    new ReverseRelationOpValidator()));

    //endregion
}
