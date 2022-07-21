package org.opensearch.graph.epb.plan;



import org.opensearch.graph.model.execution.plan.IPlan;
import org.opensearch.graph.model.execution.plan.planTree.PlanNode;

import java.util.Optional;

public interface PlanNodeWrapper<P extends IPlan> {
    Optional<PlanNode<P>> planNode();
}
