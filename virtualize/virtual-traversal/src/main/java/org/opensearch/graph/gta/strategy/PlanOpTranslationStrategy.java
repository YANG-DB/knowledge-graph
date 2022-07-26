package org.opensearch.graph.gta.strategy;





import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

public interface PlanOpTranslationStrategy {
    /**
     * traversal returns same instance as in parameter
     * @param context
     * @param traversal - is mutated parameter
     * @return
     */
    GraphTraversal<?, ?> translate(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOp planOp, TranslationContext context);
}
