package org.opensearch.graph.epb.plan.validation.opValidator;

import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.validation.ValidationResult;
import org.junit.Assert;
import org.junit.Test;

import static org.opensearch.graph.model.OntologyTestUtils.OWN;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.rel;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.typed;
import static org.opensearch.graph.model.query.Rel.Direction.R;

public class SingleEntityJoinValidatorTests {

    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, OntologyTestUtils.PERSON.type,"A"))
                .next(rel(2,OWN.getrType(),R))
                .next(typed(3, OntologyTestUtils.DRAGON.type,"B")).build();
    }

    public static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, OntologyTestUtils.PERSON.type,"A"))
                .next(rel(2,OWN.getrType(),R))
                .next(typed(3, OntologyTestUtils.DRAGON.type,"B"))
                .next(rel(4,OWN.getrType(),R))
                .next(typed(5, OntologyTestUtils.DRAGON.type,"C")).build();
    }

    @Test
    public void invalidPlanTest(){
        AsgQuery query = simpleQuery1("q","ont");
        Plan plan = new Plan(new EntityJoinOp(
                new Plan(new EntityOp(AsgQueryUtil.element$(query, 1))),
                new Plan(new EntityOp(AsgQueryUtil.element$(query, 3)),new RelationOp(AsgQueryUtil.element$(query, 2)),new EntityOp(AsgQueryUtil.element$(query, 1)))
        ));

        SingleEntityJoinValidator validator = new SingleEntityJoinValidator();
        ValidationResult planOpValid = validator.isPlanOpValid(query, plan, 0);
        Assert.assertFalse(planOpValid.valid());
    }

    @Test
    public void validPlanTest(){
        AsgQuery query = simpleQuery2("q","ont");
        Plan plan = new Plan(new EntityJoinOp(
                new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)),new RelationOp(AsgQueryUtil.element$(query, 2)),new EntityOp(AsgQueryUtil.element$(query, 3))),
                new Plan(new EntityOp(AsgQueryUtil.element$(query, 5)),new RelationOp(AsgQueryUtil.element$(query, 4)),new EntityOp(AsgQueryUtil.element$(query, 3)))
        ));

        SingleEntityJoinValidator validator = new SingleEntityJoinValidator();

        Assert.assertTrue(validator.isPlanOpValid(query, plan, 0).valid());
    }
}
