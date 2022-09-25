package org.opensearch.graph.gta.strategy.discrete;

import org.opensearch.graph.dispatcher.gta.PlanTraversalTranslator;
import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.gta.strategy.common.JoinEntityOpTranslationStrategy;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.CountEstimatesCost;
import org.opensearch.graph.model.execution.plan.costs.DoubleCost;
import org.opensearch.graph.model.execution.plan.costs.JoinCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.Start;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.unipop.process.JoinStep;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.opensearch.graph.model.query.Rel.Direction.R;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class JoinEntityOpTranslationStrategyTest {

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
    public void branchTranslationTest(){
        PlanTraversalTranslator traversalTranslatorMock = Mockito.mock(PlanTraversalTranslator.class);

        JoinEntityOpTranslationStrategy strategy = new JoinEntityOpTranslationStrategy(traversalTranslatorMock, EntityJoinOp.class);

        when(traversalTranslatorMock.translate(any(),any())).thenAnswer(invocationOnMock -> __.start().V("A"));
        EntityJoinOp joinOp = new EntityJoinOp();
        TranslationContext context = Mockito.mock(TranslationContext.class);

        when(context.getGraphTraversalSource()).thenReturn(new GraphTraversalSource(Mockito.mock(Graph.class)));
        GraphTraversal graphTraversal = strategy.translate(__.start(),
                new PlanWithCost<>(new Plan(joinOp),
                        new PlanDetailedCost(new DoubleCost(0), Collections.singleton(new PlanWithCost<>(new Plan(joinOp),
                                new JoinCost(0, 0, new PlanDetailedCost(new DoubleCost(0), Collections.singleton(new PlanWithCost<>(new Plan(), new CountEstimatesCost(1, 1)))),
                                        new PlanDetailedCost(new DoubleCost(0), Collections.singleton(new PlanWithCost<>(new Plan(), new CountEstimatesCost(1, 1)))))))))
                , joinOp, context);
        JoinStep<Object, Element> joinStep = new JoinStep<>(__.start().asAdmin());
        joinStep.addLocalChild(__.start().V("A").asAdmin());
        joinStep.addLocalChild(__.start().V("A").asAdmin());

        Assert.assertEquals(__.start().asAdmin().addStep(joinStep), graphTraversal);
    }

    @Test
    public void branchTranslation2Test(){
        AsgQuery asgQuery = simpleQuery1("","");
        PlanTraversalTranslator traversalTranslatorMock = Mockito.mock(PlanTraversalTranslator.class);

        JoinEntityOpTranslationStrategy strategy = new JoinEntityOpTranslationStrategy(traversalTranslatorMock, EntityJoinOp.class);
        EntityJoinOp joinOp = new EntityJoinOp(new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 1)))
                , new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 3))));

        when(traversalTranslatorMock.translate(any(),any())).thenAnswer(invocationOnMock -> {
            PlanWithCost<Plan, PlanDetailedCost> plan = invocationOnMock.getArgument(0);
            if(plan.equals(joinOp.getLeftBranch()))
                return __.start().V("A");
            return __.start().V("B");
        });

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getGraphTraversalSource()).thenReturn(new GraphTraversalSource(Mockito.mock(Graph.class)));
        GraphTraversal graphTraversal = strategy.translate(__.start(),
                new PlanWithCost<>(new Plan(joinOp),
                        new PlanDetailedCost(new DoubleCost(0), Collections.singleton(new PlanWithCost<>(new Plan(joinOp),
                                new JoinCost(0, 0, new PlanDetailedCost(new DoubleCost(0), Collections.singleton(new PlanWithCost<>(new Plan(), new CountEstimatesCost(1, 1)))),
                                        new PlanDetailedCost(new DoubleCost(0), Collections.singleton(new PlanWithCost<>(new Plan(), new CountEstimatesCost(1, 1)))))))))
                , joinOp, context);
        JoinStep<Object, Element> joinStep = new JoinStep<>(__.start().asAdmin());
        joinStep.addLocalChild(__.start().V("A").asAdmin());
        joinStep.addLocalChild(__.start().V("B").asAdmin());

        Assert.assertEquals(__.start().asAdmin().addStep(joinStep), graphTraversal);
    }

    @Test
    public void branchTranslationSwapTest(){
        AsgQuery asgQuery = simpleQuery1("","");
        PlanTraversalTranslator traversalTranslatorMock = Mockito.mock(PlanTraversalTranslator.class);

        JoinEntityOpTranslationStrategy strategy = new JoinEntityOpTranslationStrategy(traversalTranslatorMock, EntityJoinOp.class);
        EntityJoinOp joinOp = new EntityJoinOp(new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 1)))
                , new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 3))));

        when(traversalTranslatorMock.translate(any(),any())).thenAnswer(invocationOnMock -> {
            PlanWithCost<Plan, PlanDetailedCost> plan = invocationOnMock.getArgument(0);
            if(plan.equals(joinOp.getLeftBranch()))
                return __.start().V("A");
            return __.start().V("B");
        });

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getGraphTraversalSource()).thenReturn(new GraphTraversalSource(Mockito.mock(Graph.class)));
        GraphTraversal graphTraversal = strategy.translate(__.start(),
                new PlanWithCost<>(new Plan(joinOp),
                        new PlanDetailedCost(new DoubleCost(0), Collections.singleton(new PlanWithCost<>(new Plan(joinOp),
                                new JoinCost(0, 0, new PlanDetailedCost(new DoubleCost(0), Collections.singleton(new PlanWithCost<>(new Plan(), new CountEstimatesCost(1, 10)))),
                                        new PlanDetailedCost(new DoubleCost(0), Collections.singleton(new PlanWithCost<>(new Plan(), new CountEstimatesCost(1, 1)))))))))
                , joinOp, context);
        JoinStep<Object, Element> joinStep = new JoinStep<>(__.start().asAdmin());
        joinStep.addLocalChild(__.start().V("B").asAdmin());
        joinStep.addLocalChild(__.start().V("A").asAdmin());

        Assert.assertEquals(__.start().asAdmin().addStep(joinStep), graphTraversal);
    }
}
