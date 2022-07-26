package org.opensearch.graph.gta.translation;





import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.gta.PlanTraversalTranslator;
import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.gta.strategy.*;
import org.opensearch.graph.model.execution.plan.*;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.unipop.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.opensearch.graph.gta.strategy.PlanOpTranslationStrategy;

public class ChainedPlanOpTraversalTranslator implements PlanTraversalTranslator {
    //region Constructors
    @Inject
    public ChainedPlanOpTraversalTranslator(PlanOpTranslationStrategy translationStrategy) {
        this.translationStrategy = translationStrategy;
        this.startFrom = 0;
    }

    public ChainedPlanOpTraversalTranslator(PlanOpTranslationStrategy translationStrategy, int startFrom) {
        this.translationStrategy = translationStrategy;
        this.startFrom = startFrom;
    }
    //endregion

    //region PlanTraversalTranslator Implementation
    public GraphTraversal<?, ?> translate(PlanWithCost<Plan, PlanDetailedCost> planWithCost, TranslationContext context) {
        GraphTraversal traversal = __.start();
        for (int planOpIndex = this.startFrom; planOpIndex < planWithCost.getPlan().getOps().size(); planOpIndex++) {
            traversal = this.translationStrategy.translate(traversal, planWithCost, planWithCost.getPlan().getOps().get(planOpIndex), context);
        }

        return traversal;
    }
    //endregion

    //region Fields
    private PlanOpTranslationStrategy translationStrategy;
    private int startFrom;
    //endregion
}
