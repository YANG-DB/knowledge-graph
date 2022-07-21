package org.opensearch.graph.epb.plan.pruners;



import org.opensearch.graph.dispatcher.epb.PlanPruneStrategy;
import org.opensearch.graph.model.execution.plan.IPlan;

public class NoPruningPruneStrategy<P extends IPlan> implements PlanPruneStrategy<P> {
    @Override
    public Iterable<P> prunePlans(Iterable<P> plans) {
        return plans;
    }
}
