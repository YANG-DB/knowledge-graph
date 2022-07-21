package org.openserach.graph.asg.translator.cypher.strategies.expressions;



import com.bpodgursky.jbool_expressions.Expression;
import org.openserach.graph.asg.translator.cypher.strategies.CypherStrategyContext;
import org.opensearch.graph.model.asgQuery.AsgQuery;

import java.util.Optional;

public class ReturnVariableExpression extends BaseExpressionStrategy {

    @Override
    public void apply(Optional<Expression> parent, Expression expression, AsgQuery query, CypherStrategyContext context) {
        //todo when AliasVariable replace all tags with that specific alias
        //todo when UnAliasVariable
    }

    @Override
    public boolean isApply(Expression expression) {
        return (expression instanceof com.bpodgursky.jbool_expressions.Variable);
    }
}
