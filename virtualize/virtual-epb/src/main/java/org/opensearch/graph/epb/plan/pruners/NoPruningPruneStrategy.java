package org.opensearch.graph.epb.plan.pruners;


import org.opensearch.graph.dispatcher.epb.PlanPruneStrategy;
import org.opensearch.graph.model.execution.plan.IPlan;

/**
 * Created by moti on 2/23/2017.
 */
public class NoPruningPruneStrategy<P extends IPlan> implements PlanPruneStrategy<P> {
    @Override
    public Iterable<P> prunePlans(Iterable<P> plans) {
        return plans;
    }
}
