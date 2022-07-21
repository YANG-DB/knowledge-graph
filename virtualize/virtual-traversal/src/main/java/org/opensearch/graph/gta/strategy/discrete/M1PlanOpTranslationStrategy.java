package org.opensearch.graph.gta.strategy.discrete;



import org.opensearch.graph.gta.strategy.common.CompositePlanOpTranslationStrategy;
import org.opensearch.graph.gta.strategy.common.EntityTranslationOptions;
import org.opensearch.graph.gta.strategy.common.GoToEntityOpTranslationStrategy;
import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.gta.strategy.common.UnionOpTranslationStrategy;
import org.opensearch.graph.model.execution.plan.*;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

public class M1PlanOpTranslationStrategy extends CompositePlanOpTranslationStrategy {
    //region Constructors
    public M1PlanOpTranslationStrategy() {
        super();

        this.strategies = Stream.of(
                new EntityOpTranslationStrategy(EntityTranslationOptions.none),
                new CompositePlanOpTranslationStrategy(
                        new EntityFilterOpTranslationStrategy(EntityTranslationOptions.none),
                        new AggregationFilterOpTranslationStrategy(EntityTranslationOptions.none),
                        new EntitySelectionTranslationStrategy()
//                        new WhereByOpTranslationStrategy()
                ),
                new GoToEntityOpTranslationStrategy(),
                new RelationOpTranslationStrategy(),
                new CompositePlanOpTranslationStrategy(
                        new RelationFilterOpTranslationStrategy(),
                        new AggregationFilterOpTranslationStrategy(EntityTranslationOptions.none),
                        new RelationSelectionTranslationStrategy()
//                        new WhereByOpTranslationStrategy()
                ),
                new OptionalOpTranslationStrategy(this),
                new CountOpTranslationStrategy(this),
                new UnionOpTranslationStrategy(this)
        ).toJavaList();
    }
    //endregion


    @Override
    public GraphTraversal translate(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOp planOp, TranslationContext context) {
        return super.translate(traversal, plan, planOp, context);
    }
}
