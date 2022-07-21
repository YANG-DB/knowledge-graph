package org.openserach.graph.asg.strategy;




import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;

/**
 * Created by lior.perry on 27/02/2017.
 */
public interface AsgStrategy {
    void apply(AsgQuery query, AsgStrategyContext context);
}
