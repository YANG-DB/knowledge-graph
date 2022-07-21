package org.openserach.graph.asg.translator.cypher.strategies.expressions;



import org.openserach.graph.asg.translator.cypher.strategies.CypherElementTranslatorStrategy;
import org.openserach.graph.asg.translator.cypher.strategies.CypherStrategyContext;
import org.openserach.graph.asg.translator.cypher.strategies.CypherUtils;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opencypher.v9_0.ast.Where;

import java.util.Optional;

public class WhereClauseNodeCypherTranslator implements CypherElementTranslatorStrategy<Where> {

    public WhereClauseNodeCypherTranslator(Iterable<ExpressionStrategies> strategies) {
        this.strategies = strategies;
    }

    public void apply(Where where, AsgQuery query, CypherStrategyContext context) {
        final com.bpodgursky.jbool_expressions.Expression c = CypherUtils.reWrite(where.expression());
        strategies.forEach(s->{
            if(s.isApply(c)) s.apply(Optional.empty(), c, query, context);
        });
    }

    private Iterable<ExpressionStrategies> strategies;
}
