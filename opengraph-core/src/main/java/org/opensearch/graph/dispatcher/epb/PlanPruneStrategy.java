package org.opensearch.graph.dispatcher.epb;





import org.opensearch.graph.model.execution.plan.IPlan;

public interface PlanPruneStrategy <P extends IPlan> {
    Iterable<P> prunePlans(Iterable<P> plans);
}
