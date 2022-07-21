package org.opensearch.graph.epb.plan.modules;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Binder;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import org.opensearch.graph.dispatcher.epb.CostEstimator;
import org.opensearch.graph.dispatcher.epb.PlanExtensionStrategy;
import org.opensearch.graph.dispatcher.epb.PlanPruneStrategy;
import org.opensearch.graph.dispatcher.epb.PlanTracer;
import org.opensearch.graph.epb.plan.estimation.IncrementalEstimationContext;
import org.opensearch.graph.epb.plan.estimation.cache.*;
import org.opensearch.graph.epb.plan.estimation.count.CountCostEstimator;
import org.opensearch.graph.epb.plan.estimation.pattern.PredicateCostEstimator;
import org.opensearch.graph.epb.plan.pruners.CheapestPlanPruneStrategy;
import org.opensearch.graph.epb.plan.statistics.StatisticsProviderFactory;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.descriptors.CompositeDescriptor;
import org.opensearch.graph.model.descriptors.Descriptor;
import org.opensearch.graph.model.descriptors.ToStringDescriptor;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.composite.descriptors.*;
import org.opensearch.graph.model.execution.plan.composite.descriptors.EntityOpDescriptor;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import com.typesafe.config.Config;
import org.jooby.Env;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static com.google.inject.name.Names.named;

public class EpbDfsCountBasedRedundantModule  extends BaseEpbModule {
    //region Private Methods
    @Override
    protected Class<? extends PlanExtensionStrategy<Plan, AsgQuery>> planExtensionStrategy(Config conf) throws ClassNotFoundException {
        return (Class<? extends PlanExtensionStrategy<Plan, AsgQuery>>) Class.forName(conf.getString(conf.getString("assembly") + ".plan_extension_strategy_class"));
    }

    @Override
    protected void bindCostEstimator(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                IterablePlanOpDescriptor iterablePlanOpDescriptor = new IterablePlanOpDescriptor(IterablePlanOpDescriptor.Mode.full, null);
                Map<Class<?>, Descriptor<? extends PlanOp>> descriptors = new HashMap<>();
                descriptors.put(EntityOp.class, new EntityOpDescriptor());
                descriptors.put(EntityFilterOp.class, new EntityFilterOpDescriptor());
                iterablePlanOpDescriptor.setCompositeDescriptor(new CompositeDescriptor<>(descriptors, new ToStringDescriptor<>()));

                this.bind(new TypeLiteral<Descriptor<Plan>>() {})
                        .annotatedWith(named(CachedCostEstimator.descriptorParameter))
                        .toInstance((plan) -> iterablePlanOpDescriptor.describe(plan.getOps()));

                this.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {})
                        .annotatedWith(named(CachedCostEstimator.costEstimatorParameter))
                        .to(CountCostEstimator.class);
                this.bind(new TypeLiteral<Cache<String, PlanDetailedCost>>() {})
                        .annotatedWith(named(CachedCostEstimator.cacheParameter))
                        .toInstance(Caffeine.newBuilder()
                                .expireAfterAccess(Duration.ofMinutes(10))
                                .maximumSize(10000)
                                .build());

                this.bind(new TypeLiteral<Predicate<Plan>>() {})
                        .annotatedWith(named(PredicateCostEstimator.planPredicateParameter))
                        .toInstance(plan -> plan.getOps().size() <= 2);
                this.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {})
                        .annotatedWith(named(PredicateCostEstimator.trueCostEstimatorParameter))
                        .to(new TypeLiteral<CachedCostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {});
                this.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {})
                        .annotatedWith(named(PredicateCostEstimator.falseCostEstimatorParameter))
                        .toInstance((plan, context) -> new PlanWithCost<>(plan, context.getPreviousCost().get().getCost()));

                this.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {})
                        .annotatedWith(named(PlanTracer.Estimator.Provider.costEstimatorParameter))
                        .to(new TypeLiteral<PredicateCostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {});
                this.bindConstant().annotatedWith(named(PlanTracer.Estimator.Provider.costEstimatorNameParameter)).to(PredicateCostEstimator.class.getSimpleName());
                this.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {})
                        .toProvider(new TypeLiteral<PlanTracer.Estimator.Provider<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {});

                try {
                    this.bind(StatisticsProviderFactory.class).to(getStatisticsProviderFactory(conf)).asEagerSingleton();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                this.expose(StatisticsProviderFactory.class);
                this.expose(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>() {});
            }
        });
    }

    @Override
    protected PlanPruneStrategy<PlanWithCost<Plan,PlanDetailedCost>> globalPrunerStrategy(Config config) {
        return new CheapestPlanPruneStrategy();
    }


    protected Class<? extends StatisticsProviderFactory> getStatisticsProviderFactory(Config conf) throws ClassNotFoundException {
        return (Class<? extends StatisticsProviderFactory>) Class.forName(conf.getString(conf.getString("assembly") + ".stats_provider_class"));
    }

//endregion
}
