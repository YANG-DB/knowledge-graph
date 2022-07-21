package org.openserach.graph.asg.translator.cypher.strategies.expressions;



import org.openserach.graph.asg.translator.cypher.strategies.CypherStrategyContext;
import org.opensearch.graph.model.asgQuery.AsgQuery;

import java.util.Optional;

public interface ExpressionStrategies {
    void apply(Optional<com.bpodgursky.jbool_expressions.Expression> parent, com.bpodgursky.jbool_expressions.Expression expression, AsgQuery query, CypherStrategyContext context);
    boolean isApply(com.bpodgursky.jbool_expressions.Expression expression);

}
