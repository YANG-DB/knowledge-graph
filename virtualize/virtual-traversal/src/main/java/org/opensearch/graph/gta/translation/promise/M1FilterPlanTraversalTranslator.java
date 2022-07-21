package org.opensearch.graph.gta.translation.promise;



import org.opensearch.graph.gta.strategy.promise.M1FilterPlanOpTranslationStrategy;
import org.opensearch.graph.gta.translation.ChainedPlanOpTraversalTranslator;
import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

public class M1FilterPlanTraversalTranslator extends ChainedPlanOpTraversalTranslator {
    //region Constructors
    public M1FilterPlanTraversalTranslator() {
        super(new M1FilterPlanOpTranslationStrategy());
    }
    //endregion

    //region Override Methods
    @Override
    public GraphTraversal<?, ?> translate(PlanWithCost<Plan, PlanDetailedCost> plan, TranslationContext context) {
        return super.translate(plan, context);
    }
    //endregion
}
