package org.openserach.graph.asg.strategy;




import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;

import java.util.Arrays;

public class AsgStrategyContainer implements AsgStrategy {

    public AsgStrategyContainer(AsgStrategy ... strategies) {
        this.strategies = strategies;
    }

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Arrays.asList(strategies).forEach(p->p.apply(query,context));
    }

    private AsgStrategy[] strategies;
}
