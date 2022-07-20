package org.opensearch.graph.epb.plan.modules;

/*-
 * #%L
 * virtual-epb
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.inject.Binder;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import org.opensearch.graph.dispatcher.epb.CostEstimator;
import org.opensearch.graph.dispatcher.epb.PlanExtensionStrategy;
import org.opensearch.graph.dispatcher.epb.PlanSelector;
import org.opensearch.graph.dispatcher.epb.PlanTracer;
import org.opensearch.graph.epb.plan.estimation.CostEstimationConfig;
import org.opensearch.graph.epb.plan.estimation.IncrementalEstimationContext;
import org.opensearch.graph.epb.plan.estimation.pattern.RegexPatternCostEstimator;
import org.opensearch.graph.epb.plan.estimation.pattern.estimators.M1PatternCostEstimator;
import org.opensearch.graph.epb.plan.estimation.pattern.estimators.PatternCostEstimator;
import org.opensearch.graph.epb.plan.extenders.M1.M1PlanExtensionStrategy;
import org.opensearch.graph.epb.plan.selectors.CheapestPlanSelector;
import org.opensearch.graph.epb.plan.statistics.EBaseStatisticsProviderFactory;
import org.opensearch.graph.epb.plan.statistics.GraphStatisticsProvider;
import org.opensearch.graph.epb.plan.statistics.StatisticsProviderFactory;
import org.opensearch.graph.epb.plan.statistics.configuration.StatConfig;
import org.opensearch.graph.epb.plan.statistics.provider.EngineStatDocumentProvider;
import org.opensearch.graph.epb.plan.statistics.provider.EngineStatisticsGraphProvider;
import org.opensearch.graph.epb.plan.statistics.provider.StatDataProvider;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.CountEstimatesCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import com.typesafe.config.Config;
import org.jooby.Env;

import static com.google.inject.name.Names.named;

/**
 * Created by lior.perry on 22/02/2017.
 */
public class EpbModule extends BaseEpbModule {

    @Override
    protected PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> globalPlanSelector(Config config) {
        return new CheapestPlanSelector();
    }

    protected void bindCostEstimator(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(StatConfig.class).toInstance(new StatConfig(conf));
                this.bind(GraphStatisticsProvider.class).to(EngineStatisticsGraphProvider.class).asEagerSingleton();
                this.bind(StatDataProvider.class).to(EngineStatDocumentProvider.class).asEagerSingleton();

                this.bind(StatisticsProviderFactory.class).to(EBaseStatisticsProviderFactory.class).asEagerSingleton();

                this.bind(CostEstimationConfig.class)
                        .toInstance(new CostEstimationConfig(conf.getDouble("epb.cost.alpha"), conf.getDouble("epb.cost.delta")));
                this.bind(new TypeLiteral<PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>(){})
                        .to(M1PatternCostEstimator.class).asEagerSingleton();

                this.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>(){})
                        .annotatedWith(named(PlanTracer.Estimator.Provider.costEstimatorParameter))
                        .to(RegexPatternCostEstimator.class).asEagerSingleton();
                this.bindConstant().annotatedWith(named(PlanTracer.Estimator.Provider.costEstimatorNameParameter)).to(RegexPatternCostEstimator.class.getSimpleName());
                this.bind(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>(){})
                        .toProvider(new TypeLiteral<PlanTracer.Estimator.Provider<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>(){});

                this.expose(StatisticsProviderFactory.class);
                this.expose(new TypeLiteral<CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>>(){});
            }
        });
    }

    @Override
    protected Class<? extends PlanExtensionStrategy<Plan, AsgQuery>> planExtensionStrategy(Config config) {
        return M1PlanExtensionStrategy.class;
    }

//endregion
}
