package org.opensearch.graph.epb.plan.validation.opValidator;

import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.Start;
import org.opensearch.graph.model.query.entity.ETyped;
import org.junit.Assert;
import org.junit.Test;

import static org.opensearch.graph.model.query.Rel.Direction.R;

public class JoinCompletePlanOpValidatorTests {
    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        Start start = new Start();
        start.seteNum(0);

        ETyped eTyped = new ETyped();
        eTyped.seteNum(1);
        eTyped.seteTag("A");
        eTyped.seteType("1");

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setDir(R);
        rel.setrType("1");

        ETyped eTyped2 = new ETyped();
        eTyped2.seteNum(3);
        eTyped2.seteTag("B");
        eTyped2.seteType("2");

        Rel rel2 = new Rel();
        rel2.seteNum(4);
        rel2.setDir(R);
        rel2.setrType("1");

        ETyped eTyped3 = new ETyped();
        eTyped3.seteNum(5);
        eTyped3.seteTag("C");
        eTyped3.seteType("2");

        AsgEBase<Start> asgStart =
                AsgEBase.Builder.<Start>get().withEBase(start)
                        .withNext(AsgEBase.Builder.get().withEBase(eTyped)
                                .withNext(AsgEBase.Builder.get().withEBase(rel)
                                        .withNext(AsgEBase.Builder.get().withEBase(eTyped2)
                                                .withNext(AsgEBase.Builder.get().withEBase(rel2)
                                                        .withNext(AsgEBase.Builder.get().withEBase(eTyped3).build())
                                                    .build())
                                                .build())
                                        .build())
                                .build())
                        .build();

        return AsgQuery.AsgQueryBuilder.anAsgQuery().withName(queryName).withOnt(ontologyName).withStart(asgStart).build();
    }

    @Test
    public void completePlanValidTest(){
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan left = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 3)));
        Plan right = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 1)), new RelationOp(AsgQueryUtil.element$(asgQuery,2)), new EntityOp(AsgQueryUtil.element$(asgQuery, 3)));
        EntityJoinOp join = new EntityJoinOp(left, right, true);

        JoinCompletePlanOpValidator validator = new JoinCompletePlanOpValidator();
        Assert.assertTrue(validator.isPlanOpValid(asgQuery, new Plan(join, new RelationOp(AsgQueryUtil.element$(asgQuery, 4)), new EntityOp(AsgQueryUtil.element$(asgQuery, 5))), 0).valid());

    }

    @Test
    public void invalidJoinPlanTest(){
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan left = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 3)));
        Plan right = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 1)));
        EntityJoinOp join = new EntityJoinOp(left, right);

        JoinCompletePlanOpValidator validator = new JoinCompletePlanOpValidator();
        Assert.assertFalse(validator.isPlanOpValid(asgQuery, new Plan(join, new RelationOp(AsgQueryUtil.element$(asgQuery, 4)), new EntityOp(AsgQueryUtil.element$(asgQuery, 5))), 0).valid());

    }
}
