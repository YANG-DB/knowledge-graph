package org.opensearch.graph.gta.strategy.promise;


import org.opensearch.graph.gta.strategy.common.CompositePlanOpTranslationStrategy;
import org.opensearch.graph.gta.strategy.common.EntityTranslationOptions;
import org.opensearch.graph.gta.strategy.common.GoToEntityOpTranslationStrategy;
import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.model.execution.plan.*;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

public class M1PlanOpTranslationStrategy extends CompositePlanOpTranslationStrategy {
    //region Constructors
    public M1PlanOpTranslationStrategy() {
        super(
                new EntityOpTranslationStrategy(EntityTranslationOptions.none),
                new GoToEntityOpTranslationStrategy(),
                new RelationOpTranslationStrategy(),
                new CompositePlanOpTranslationStrategy(
                        new EntityFilterOpTranslationStrategy(EntityTranslationOptions.none),
                        new EntitySelectionTranslationStrategy()),
                new RelationFilterOpTranslationStrategy());
    }
    //endregion


    @Override
    public GraphTraversal translate(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOp planOp, TranslationContext context) {
        return super.translate(traversal, plan, planOp, context);
    }
}
