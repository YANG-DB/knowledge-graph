package org.openserach.graph.asg.strategy;







import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;

public interface AsgStrategy {
    void apply(AsgQuery query, AsgStrategyContext context);
}
