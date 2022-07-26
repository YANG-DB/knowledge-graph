package org.opensearch.graph.gta.strategy.discrete;





import org.opensearch.graph.gta.strategy.PlanOpTranslationStrategy;
import org.opensearch.graph.gta.strategy.PlanOpTranslationStrategyBase;
import org.opensearch.graph.gta.translation.ChainedPlanOpTraversalTranslator;
import org.opensearch.graph.dispatcher.gta.PlanTraversalTranslator;
import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.model.execution.plan.composite.OptionalOp;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

public class OptionalOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    //region Constructors
    public OptionalOpTranslationStrategy(PlanOpTranslationStrategy planOpTranslationStrategy) {
        super(planOp -> planOp.getClass().equals(OptionalOp.class));
        this.planOpTranslationStrategy = planOpTranslationStrategy;
    }
    //endregion

    //region PlanOpTranslationStrategyBase Implementation
    @Override
    protected GraphTraversal<?, ?> translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> planWithCost, PlanOp planOp, TranslationContext context) {
        OptionalOp optionalOp = (OptionalOp)planOp;

        int indexOfOptional = planWithCost.getPlan().getOps().indexOf(planOp);
        Plan optionalPlan = new Plan(Stream.ofAll(planWithCost.getPlan().getOps()).take(indexOfOptional).toJavaList()).append(optionalOp);

        PlanTraversalTranslator planTraversalTranslator =
                new ChainedPlanOpTraversalTranslator(this.planOpTranslationStrategy, indexOfOptional);

        GraphTraversal<?, ?> optionalTraversal = planTraversalTranslator.translate(new PlanWithCost<>(optionalPlan, planWithCost.getCost()), context);
        return traversal.optional(optionalTraversal);
    }
    //endregion

    //region Fields
    private PlanOpTranslationStrategy planOpTranslationStrategy;
    //endregion
}
