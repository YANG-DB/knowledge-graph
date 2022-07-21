package org.opensearch.graph.epb.plan.modules;



import org.opensearch.graph.dispatcher.epb.PlanExtensionStrategy;
import org.opensearch.graph.epb.plan.extenders.M1.M1DfsRedundantPlanExtensionStrategy;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import com.typesafe.config.Config;

public class EpbDfsRedundantModule extends BaseEpbModule {
    //region Private Methods
    @Override
    protected Class<? extends PlanExtensionStrategy<Plan, AsgQuery>> planExtensionStrategy(Config config) {
        return M1DfsRedundantPlanExtensionStrategy.class;
    }
    //endregion
}
