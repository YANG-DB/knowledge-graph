package org.opensearch.graph.gta.strategy.common;





import org.opensearch.graph.gta.strategy.PlanOpTranslationStrategyBase;
import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.model.execution.plan.entity.GoToEntityOp;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

public class GoToEntityOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    //region Constructors
    //endregion
    public GoToEntityOpTranslationStrategy() {
        super(GoToEntityOp.class);
    }

    //region PlanOpTranslationStrategy Implementation
    @Override
    protected GraphTraversal translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOp planOp, TranslationContext context) {
        return traversal.select(((GoToEntityOp)planOp).getAsgEbase().geteBase().geteTag());
    }
    //endregion
}
