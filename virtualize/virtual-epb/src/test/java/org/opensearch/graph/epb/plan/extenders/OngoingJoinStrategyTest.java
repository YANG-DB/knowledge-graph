package org.opensearch.graph.epb.plan.extenders;

import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.epb.plan.extenders.M1.M1NonRedundantPlanExtensionStrategy;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.Start;
import org.opensearch.graph.model.query.entity.ETyped;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.Optional;

import static org.opensearch.graph.model.query.Rel.Direction.R;

/**
 * Created by moti on 04/07/2017.
 */
public class OngoingJoinStrategyTest {
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

        AsgEBase<Start> asgStart =
                AsgEBase.Builder.<Start>get().withEBase(start)
                        .withNext(AsgEBase.Builder.get().withEBase(eTyped)
                                .withNext(AsgEBase.Builder.get().withEBase(rel)
                                        .withNext(AsgEBase.Builder.get().withEBase(eTyped2)
                                                .build())
                                        .build())
                                .build())
                        .build();

        return AsgQuery.AsgQueryBuilder.anAsgQuery().withName(queryName).withOnt(ontologyName).withStart(asgStart).build();
    }

    @Test
    public void emptyPlanTest(){
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        M1NonRedundantPlanExtensionStrategy m1Strategy = new M1NonRedundantPlanExtensionStrategy();
        JoinOngoingExtensionStrategy join = new JoinOngoingExtensionStrategy(m1Strategy);
        Iterable<Plan> plans = join.extendPlan(Optional.empty(), asgQuery);
        Assert.assertEquals(0, Stream.ofAll(plans).length());
    }

    @Test
    public void singleJoinPlanTest(){
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 1)));

        M1NonRedundantPlanExtensionStrategy m1ExtensionStrategy = new M1NonRedundantPlanExtensionStrategy();

        JoinSeedExtensionStrategy join = new JoinSeedExtensionStrategy(m1ExtensionStrategy);
        Iterable<Plan> plans = join.extendPlan(Optional.of(plan), asgQuery);

        JoinOngoingExtensionStrategy joinOngoing = new JoinOngoingExtensionStrategy(m1ExtensionStrategy);
        Iterator<Plan> iterator = plans.iterator();
        Plan firstPlan = iterator.next();
        ((EntityJoinOp)firstPlan.getOps().get(0)).setComplete(true);
        Iterable<Plan> joinExtensionPlans = joinOngoing.extendPlan(Optional.of(firstPlan), asgQuery);

        Assert.assertEquals(0, Stream.ofAll(joinExtensionPlans).length());

        joinExtensionPlans = joinOngoing.extendPlan(Optional.of(iterator.next()), asgQuery);

        Assert.assertEquals(2, Stream.ofAll(joinExtensionPlans).length());
        Iterator<Plan> planIterator = joinExtensionPlans.iterator();
        Assert.assertEquals(planIterator.next().toString(), planIterator.next().toString());
    }

    @Test
    public void singleEntityPlanTest(){
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 1)));
        M1NonRedundantPlanExtensionStrategy m1Strategy = new M1NonRedundantPlanExtensionStrategy();
        JoinOngoingExtensionStrategy joinOngoing = new JoinOngoingExtensionStrategy(m1Strategy);
        Iterable<Plan> plans = joinOngoing.extendPlan(Optional.of(plan), asgQuery);
        Assert.assertEquals(0, Stream.ofAll(plans).length());
    }

}
