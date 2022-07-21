package org.opensearch.graph.epb.plan;


import org.opensearch.graph.model.execution.plan.IPlan;
import org.opensearch.graph.model.execution.plan.planTree.PlanNode;

import java.util.Optional;

/**
 * Created by lior.perry on 6/25/2017.
 */
public interface PlanNodeWrapper<P extends IPlan> {
    Optional<PlanNode<P>> planNode();
}
