package org.opensearch.graph.services.engine2.mocks;

import com.google.inject.Binder;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import org.opensearch.graph.dispatcher.epb.PlanSearcher;
import org.opensearch.graph.dispatcher.modules.ModuleBase;
import org.opensearch.graph.epb.plan.statistics.NoStatsProvider;
import org.opensearch.graph.epb.plan.statistics.StatisticsProviderFactory;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import com.typesafe.config.Config;
import org.jooby.Env;

public class EpbMockModule extends ModuleBase {
    @Override
    protected void configureInner(Env env, Config config, Binder binder) throws Throwable {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                this.bind(new TypeLiteral<PlanSearcher<Plan, PlanDetailedCost, AsgQuery>>(){}).toInstance(query -> plan);
                this.bind(StatisticsProviderFactory.class).to(NoStatsProvider.class);
                this.expose(StatisticsProviderFactory.class);
                this.expose(new TypeLiteral<PlanSearcher<Plan, PlanDetailedCost, AsgQuery>>(){});
            }
        });
    }


    public static PlanWithCost<Plan, PlanDetailedCost> plan;
}
