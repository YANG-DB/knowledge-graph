package org.opensearch.graph.epb.plan.estimation.pattern;





import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.opensearch.graph.dispatcher.epb.CostEstimator;
import org.opensearch.graph.model.execution.plan.PlanWithCost;

import java.util.function.Predicate;

public class PredicateCostEstimator<P, C, TContext> implements CostEstimator<P, C, TContext> {
    public final static String planPredicateParameter = "PredicateCostEstimator.@planPredicate";
    public final static String trueCostEstimatorParameter = "PredicateCostEstimator.@trueCostEstimator";
    public final static String falseCostEstimatorParameter = "PredicateCostEstimator.@falseCostEstimator";

    //region Constructors
    @Inject
    public PredicateCostEstimator(
            @Named(planPredicateParameter) Predicate<P> planPredicate,
            @Named(trueCostEstimatorParameter) CostEstimator<P, C, TContext> trueCostEstimator,
            @Named(falseCostEstimatorParameter) CostEstimator<P, C, TContext> falseCostEstimator) {
        this.planPredicate = planPredicate;
        this.trueCostEstimator = trueCostEstimator;
        this.falseCostEstimator = falseCostEstimator;
    }
    //endregion

    //region CostEstimator Implementation
    @Override
    public PlanWithCost<P, C> estimate(P plan, TContext context) {
        return this.planPredicate.test(plan) ?
                this.trueCostEstimator.estimate(plan, context) :
                this.falseCostEstimator.estimate(plan, context);
    }
    //endregion

    //region Fields
    private Predicate<P> planPredicate;
    private CostEstimator<P, C, TContext> trueCostEstimator;
    private CostEstimator<P, C, TContext> falseCostEstimator;
    //endregion
}
