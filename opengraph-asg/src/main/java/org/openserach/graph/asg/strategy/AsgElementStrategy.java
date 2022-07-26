package org.openserach.graph.asg.strategy;







import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.query.EBase;

public interface AsgElementStrategy<T extends EBase> {
    void apply(AsgQuery query, AsgEBase<T> element, AsgStrategyContext context);
}
