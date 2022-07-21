package org.opensearch.graph.gta.strategy;



import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.function.Predicate;

public abstract class PlanOpTranslationStrategyBase implements PlanOpTranslationStrategy {
    //region Constructors
    @SafeVarargs
    public PlanOpTranslationStrategyBase(Class<? extends PlanOp>...klasses) {
        this.klasses = klasses;
    }

    public PlanOpTranslationStrategyBase(Predicate<PlanOp> planOpPredicate) {
        this.planOpPredicate = planOpPredicate;
    }
    //endregion

    //region PlanOpTranslationStrategy Implementation
    @Override
    public GraphTraversal translate(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOp planOp, TranslationContext context) {
        if (this.planOpPredicate != null) {
            if (!this.planOpPredicate.test(planOp)) {
                return traversal;
            }
        }
        else if (Stream.of(klasses).filter(klass -> klass.isAssignableFrom(planOp.getClass())).isEmpty()) {
            return traversal;
        }

        return translateImpl(traversal, plan, planOp, context);
    }
    //endregion

    //region Abstract Methods
    protected abstract GraphTraversal translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOp planOp, TranslationContext context);
    //endregion

    //region Fields
    private Class<? extends PlanOp>[] klasses;
    private Predicate<PlanOp> planOpPredicate;
    //endregion
}
