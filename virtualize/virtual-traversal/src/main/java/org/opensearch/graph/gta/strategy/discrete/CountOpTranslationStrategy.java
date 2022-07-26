package org.opensearch.graph.gta.strategy.discrete;





import org.opensearch.graph.dispatcher.gta.PlanTraversalTranslator;
import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.gta.strategy.PlanOpTranslationStrategy;
import org.opensearch.graph.gta.strategy.PlanOpTranslationStrategyBase;
import org.opensearch.graph.gta.translation.ChainedPlanOpTraversalTranslator;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.CountOp;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

public class CountOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    //region Constructors
    public CountOpTranslationStrategy(PlanOpTranslationStrategy planOpTranslationStrategy) {
        super(planOp -> planOp.getClass().equals(CountOp.class));
        this.planOpTranslationStrategy = planOpTranslationStrategy;
    }
    //endregion

    //region PlanOpTranslationStrategyBase Implementation
    @Override
    protected GraphTraversal<?, ?> translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> planWithCost, PlanOp planOp, TranslationContext context) {

        CountOp countOp = (CountOp)planOp;

        int indexOfCount = planWithCost.getPlan().getOps().indexOf(planOp);
        Plan countPlan = new Plan(Stream.ofAll(planWithCost.getPlan().getOps()).take(indexOfCount).toJavaList()).append(countOp);

        PlanTraversalTranslator planTraversalTranslator =
                new ChainedPlanOpTraversalTranslator(this.planOpTranslationStrategy, indexOfCount);

        GraphTraversal<?, ?> countTraversal = planTraversalTranslator.translate(new PlanWithCost<>(countPlan, planWithCost.getCost()), context);

        return countTraversal.count();
    }
    //endregion

    //region Fields
    private PlanOpTranslationStrategy planOpTranslationStrategy;
    //endregion
}
