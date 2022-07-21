package org.opensearch.graph.dispatcher.epb;




import org.opensearch.graph.model.asgQuery.IQuery;
import org.opensearch.graph.model.execution.plan.IPlan;

import java.util.Optional;

public interface PlanExtensionStrategy<P extends IPlan,Q extends IQuery> {
    Iterable<P> extendPlan(Optional<P> plan, Q query);
}
