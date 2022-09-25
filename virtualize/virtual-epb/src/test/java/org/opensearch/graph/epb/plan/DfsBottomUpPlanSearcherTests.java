package org.opensearch.graph.epb.plan;

import org.opensearch.graph.dispatcher.epb.PlanPruneStrategy;
import org.opensearch.graph.dispatcher.epb.PlanSelector;
import org.opensearch.graph.dispatcher.epb.PlanValidator;
import org.opensearch.graph.dispatcher.epb.CostEstimator;
import org.opensearch.graph.epb.plan.estimation.dummy.DummyCostEstimator;
import org.opensearch.graph.epb.plan.estimation.IncrementalEstimationContext;
import org.opensearch.graph.epb.plan.extenders.CompositePlanExtensionStrategy;
import org.opensearch.graph.epb.plan.extenders.InitialPlanGeneratorExtensionStrategy;
import org.opensearch.graph.epb.plan.extenders.StepAdjacentDfsStrategy;
import org.opensearch.graph.epb.plan.pruners.NoPruningPruneStrategy;
import org.opensearch.graph.epb.plan.selectors.AllCompletePlanSelector;
import org.opensearch.graph.epb.plan.validation.M1PlanValidator;
import org.opensearch.graph.epb.utils.BuilderTestUtil;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.AsgEBaseContainer;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.query.EBase;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by moti on 2/23/2017.
 */
public class DfsBottomUpPlanSearcherTests {
    @Test
    public void TestBuilderSimplePath(){
        Pair<AsgQuery, AsgEBase<? extends EBase>> query = BuilderTestUtil.createTwoEntitiesPathQuery();

        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();


        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query.getLeft());

        Assert.assertEquals(3, plan.getPlan().getOps().size());
    }

    @Test
    public void TestBuilderSingleEntity(){

        Pair<AsgQuery, AsgEBase> query = BuilderTestUtil.createSingleEntityQuery();

        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();


        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query.getLeft());

        Assert.assertEquals(1, plan.getPlan().getOps().size());
    }

    @Test
    public void TestBuilderAllPaths() {
        Pair<AsgQuery, AsgEBase<? extends EBase>> query = BuilderTestUtil.createTwoEntitiesPathQuery();

        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();


        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query.getLeft());

        Assert.assertEquals(3, plan.getPlan().getOps().size());

        AsgEBase firstElement = query.getLeft().getStart().getNext().get(0);
        AsgEBase secondElement = (AsgEBase) firstElement.getNext().get(0);
        AsgEBase thirdElement = (AsgEBase) secondElement.getNext().get(0);
        boolean foundFirstPlan = false;

        List<PlanOp> ops = plan.getPlan().getOps();

        if (firstElement.geteNum() == ((AsgEBaseContainer) ops.get(0)).getAsgEbase().geteNum() &&
                secondElement.geteNum() == ((AsgEBaseContainer) ops.get(1)).getAsgEbase().geteNum() &&
                thirdElement.geteNum() == ((AsgEBaseContainer) ops.get(2)).getAsgEbase().geteNum()) {
            foundFirstPlan = true;
        }

        Assert.assertTrue(foundFirstPlan);
    }

    private BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> createBottomUpPlanSearcher() {
        CompositePlanExtensionStrategy<Plan, AsgQuery> compositePlanExtensionStrategy = new CompositePlanExtensionStrategy<>(
                new InitialPlanGeneratorExtensionStrategy(),
                new StepAdjacentDfsStrategy());

        PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> pruneStrategy = new NoPruningPruneStrategy<>();
        PlanValidator<Plan, AsgQuery> validator = new M1PlanValidator();

        CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> costEstimator =
                new DummyCostEstimator<>(new PlanDetailedCost());

        PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> planSelector = new AllCompletePlanSelector<>();

        return new BottomUpPlanSearcher<>(
                compositePlanExtensionStrategy,
                pruneStrategy,
                pruneStrategy,
                planSelector,
                planSelector,
                validator,
                costEstimator);
    }


}
