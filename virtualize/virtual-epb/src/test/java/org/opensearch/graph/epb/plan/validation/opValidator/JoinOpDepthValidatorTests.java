package org.opensearch.graph.epb.plan.validation.opValidator;

import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.epb.plan.validation.ChainedPlanValidator;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.junit.Assert;
import org.junit.Test;

import static org.opensearch.graph.model.OntologyTestUtils.OWN;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.rel;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.typed;
import static org.opensearch.graph.model.query.Rel.Direction.R;

public class JoinOpDepthValidatorTests {
    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, OntologyTestUtils.PERSON.type,"A"))
                .next(rel(2,OWN.getrType(),R))
                .next(typed(3, OntologyTestUtils.DRAGON.type,"B")).build();
    }

    private ChainedPlanValidator.PlanOpValidator createValidator(int depth){
        if(depth == 0){
            return new JoinOpDepthValidator(depth);
        }
        return new CompositePlanOpValidator(CompositePlanOpValidator.Mode.all,
                new JoinOpDepthValidator(3),
                new JoinOpCompositeValidator(
                        new ChainedPlanValidator(createValidator(depth-1)),
                        new ChainedPlanValidator(
                                createValidator(depth-1)))
                );


    }

    @Test
    public void testTooDeepPlan(){
        AsgQuery query = simpleQuery1("q","o");

        Plan plan = new Plan(new EntityJoinOp(
                new Plan(
                        new EntityJoinOp(
                                new Plan(
                                        new EntityJoinOp(new Plan(new EntityOp(AsgQueryUtil.element$(query, 1))),
                                                            new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)))))
                                ,new Plan(new EntityOp(AsgQueryUtil.element$(query, 1))))),
                new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)))
        ));


        ChainedPlanValidator.PlanOpValidator validator = createValidator(2);

        Assert.assertFalse(validator.isPlanOpValid(query, plan, 0).valid());
    }

    @Test
    public void testValidPlan(){
        AsgQuery query = simpleQuery1("q","o");
        Plan plan = new Plan(new EntityJoinOp(
                new Plan(
                        new EntityJoinOp(new Plan(new EntityOp(AsgQueryUtil.element$(query, 1))),new Plan(new EntityOp(AsgQueryUtil.element$(query, 1))))),
                new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)))
        ));

        ChainedPlanValidator.PlanOpValidator validator = createValidator(3);

        Assert.assertTrue(validator.isPlanOpValid(query, plan, 0).valid());
    }


}
