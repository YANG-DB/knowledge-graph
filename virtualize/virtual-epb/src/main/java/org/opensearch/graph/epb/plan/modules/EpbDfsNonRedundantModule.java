package org.opensearch.graph.epb.plan.modules;



import org.opensearch.graph.dispatcher.epb.*;
import org.opensearch.graph.epb.plan.extenders.M1.M1DfsNonRedundantPlanExtensionStrategy;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import com.typesafe.config.Config;

public class EpbDfsNonRedundantModule extends BaseEpbModule {

    //region Private Methods
    @Override
    protected Class<? extends PlanExtensionStrategy<Plan, AsgQuery>> planExtensionStrategy(Config config) {
        return M1DfsNonRedundantPlanExtensionStrategy.class;
    }
    //endregion
}
