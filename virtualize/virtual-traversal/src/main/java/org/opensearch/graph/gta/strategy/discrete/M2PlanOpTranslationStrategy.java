package org.opensearch.graph.gta.strategy.discrete;





import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.gta.strategy.common.*;
import org.opensearch.graph.gta.strategy.common.*;
import org.opensearch.graph.gta.translation.ChainedPlanOpTraversalTranslator;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

public class M2PlanOpTranslationStrategy extends CompositePlanOpTranslationStrategy {
    //region Constructors
    public M2PlanOpTranslationStrategy() {
        super();

        this.strategies = Stream.of(
                new EntityOpTranslationStrategy(EntityTranslationOptions.none),
                new CompositePlanOpTranslationStrategy(
                        new EntityFilterOpTranslationStrategy(EntityTranslationOptions.none),
                        new EntitySelectionTranslationStrategy()
//                        new WhereByOpTranslationStrategy()
                ),
                new GoToEntityOpTranslationStrategy(),
                new RelationOpTranslationStrategy(),
                new CompositePlanOpTranslationStrategy(
                        new RelationFilterOpTranslationStrategy(),
                        new RelationSelectionTranslationStrategy()
//                        new WhereByOpTranslationStrategy()
                ),
                new OptionalOpTranslationStrategy(this),
                new CountOpTranslationStrategy(this),
                new UnionOpTranslationStrategy(this),
                new JoinEntityOpTranslationStrategy(new ChainedPlanOpTraversalTranslator(this), EntityJoinOp.class)
        ).toJavaList();
    }
    //endregion


    @Override
    public GraphTraversal translate(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOp planOp, TranslationContext context) {
        return super.translate(traversal, plan, planOp, context);
    }
}
