package org.opensearch.graph.epb.plan.estimation.cache;





import com.github.benmanes.caffeine.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.opensearch.graph.dispatcher.epb.CostEstimator;
import org.opensearch.graph.model.descriptors.Descriptor;
import org.opensearch.graph.model.execution.plan.PlanWithCost;

public class CachedCostEstimator<P, C, TContext> implements CostEstimator<P, C, TContext> {
    public static final String costEstimatorParameter = "CachedCostEstimator.@costEstimator";
    public static final String cacheParameter = "CachedCostEstimator.@cache";
    public static final String descriptorParameter = "CachedCostEstimator.@descriptor";

    //region Constructors
    @Inject
    public CachedCostEstimator(
            @Named(costEstimatorParameter) CostEstimator<P, C, TContext> costEstimator,
            @Named(cacheParameter) Cache<String, C> cache,
            @Named(descriptorParameter) Descriptor<P> descriptor) {
        this.costEstimator = costEstimator;
        this.cache = cache;
        this.descriptor = descriptor;
    }
    //endregion

    //region CostEstimator Implementation
    @Override
    public PlanWithCost<P, C> estimate(P plan, TContext context) {
        return new PlanWithCost<>(plan,
                this.cache.get(
                    this.descriptor.describe(plan),
                    key -> this.costEstimator.estimate(plan, context).getCost()));
    }
    //endregion

    //region Fields
    private CostEstimator<P, C, TContext> costEstimator;
    private Cache<String, C> cache;
    private Descriptor<P> descriptor;
    //endregion
}
