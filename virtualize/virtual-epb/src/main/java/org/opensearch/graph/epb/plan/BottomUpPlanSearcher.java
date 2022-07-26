package org.opensearch.graph.epb.plan;





import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.opensearch.graph.dispatcher.epb.*;
import org.opensearch.graph.epb.plan.estimation.IncrementalEstimationContext;
import org.opensearch.graph.model.asgQuery.IQuery;
import org.opensearch.graph.model.execution.plan.IPlan;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.costs.Cost;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


public class BottomUpPlanSearcher<P extends IPlan, C extends Cost, Q extends IQuery> implements PlanSearcher<P, C, Q> {
    public static final String globalPruneStrategyParameter = "BottomUpPlanSearcher.@globalPruneStrategy";
    public static final String localPruneStrategyParameter = "BottomUpPlanSearcher.@localPruneStrategy";
    public static final String globalPlanSelectorParameter = "BottomUpPlanSearcher.@globalPlanSelector";
    public static final String localPlanSelectorParameter = "BottomUpPlanSearcher.@localPlanSelector";

    @Inject
    public BottomUpPlanSearcher(PlanExtensionStrategy<P, Q> extensionStrategy,
                                @Named(globalPruneStrategyParameter) PlanPruneStrategy<PlanWithCost<P, C>> globalPruneStrategy,
                                @Named(localPruneStrategyParameter) PlanPruneStrategy<PlanWithCost<P, C>> localPruneStrategy,
                                @Named(globalPlanSelectorParameter) PlanSelector<PlanWithCost<P, C>, Q> globalPlanSelector,
                                @Named(localPlanSelectorParameter) PlanSelector<PlanWithCost<P, C>, Q> localPlanSelector,
                                PlanValidator<P, Q> planValidator,
                                CostEstimator<P, C, IncrementalEstimationContext<P, C, Q>> costEstimator) {
        this.extensionStrategy = extensionStrategy;
        this.globalPruneStrategy = globalPruneStrategy;
        this.localPruneStrategy = localPruneStrategy;
        this.globalPlanSelector = globalPlanSelector;
        this.localPlanSelector = localPlanSelector;
        this.planValidator = planValidator;
        this.costEstimator = costEstimator;
    }

    //region Methods
    @Override
    public PlanWithCost<P, C> search(Q query) {
        Iterable<PlanWithCost<P, C>> selectedPlans = Collections.emptyList();
        List<PlanWithCost<P, C>> currentPlans = Collections.singletonList(null);

        while (currentPlans.size() > 0) {
            List<PlanWithCost<P, C>> newPlans = new ArrayList<>();
            for (PlanWithCost<P, C> partialPlan : currentPlans) {
                final IncrementalEstimationContext<P, C, Q> context = new IncrementalEstimationContext<>(Optional.ofNullable(partialPlan), query);
                Stream.ofAll(this.localPruneStrategy.prunePlans(
                    Stream.ofAll(this.extensionStrategy.extendPlan(context.getPreviousCost().map(PlanWithCost::getPlan), query))
                            .filter(extendedPlan -> this.planValidator.isPlanValid(extendedPlan, query).valid())
                            .map(validExtendedPlan -> this.costEstimator.estimate(validExtendedPlan, context))))
                        .forEach(newPlans::add);
            }

            currentPlans = Stream.ofAll(this.globalPruneStrategy.prunePlans(newPlans)).toJavaList();
            selectedPlans = Stream.ofAll(selectedPlans).appendAll(this.localPlanSelector.select(query, currentPlans)).toJavaList();
        }

        selectedPlans = this.globalPlanSelector.select(query, selectedPlans);
        return Stream.ofAll(selectedPlans).isEmpty() ? null : Stream.ofAll(selectedPlans).get(0);
    }
    //endregion

    //region Fields
    private PlanExtensionStrategy<P, Q> extensionStrategy;
    private PlanPruneStrategy<PlanWithCost<P, C>> globalPruneStrategy;
    private PlanPruneStrategy<PlanWithCost<P, C>> localPruneStrategy;
    private PlanSelector<PlanWithCost<P, C>, Q> globalPlanSelector;
    private PlanSelector<PlanWithCost<P, C>, Q> localPlanSelector;
    private PlanValidator<P, Q> planValidator;
    private CostEstimator<P, C, IncrementalEstimationContext<P, C, Q>> costEstimator;
    //endregion
}
