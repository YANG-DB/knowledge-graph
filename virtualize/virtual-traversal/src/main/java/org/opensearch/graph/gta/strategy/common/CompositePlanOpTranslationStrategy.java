package org.opensearch.graph.gta.strategy.common;


import org.opensearch.graph.gta.strategy.PlanOpTranslationStrategy;
import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

public class CompositePlanOpTranslationStrategy implements PlanOpTranslationStrategy {
    //region Constructors
    public CompositePlanOpTranslationStrategy(PlanOpTranslationStrategy...strategies) {
        this.strategies = Stream.of(strategies).toJavaList();
    }

    public CompositePlanOpTranslationStrategy(Iterable<PlanOpTranslationStrategy> strategies) {
        this.strategies = Stream.ofAll(strategies).toJavaList();
    }
    //endregion

    //region PlanOpTranslationStrategy Implementation
    @Override
    public GraphTraversal translate(GraphTraversal traversal, PlanWithCost<Plan,PlanDetailedCost> plan, PlanOp planOp, TranslationContext context) {
        for(PlanOpTranslationStrategy planOpTranslationStrategy : this.strategies) {
            traversal = planOpTranslationStrategy.translate(traversal, plan, planOp, context);
        }

        return traversal;
    }
    //endregion

    //region Fields
    protected Iterable<PlanOpTranslationStrategy> strategies;
    //endregion
}
