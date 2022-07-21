package org.openserach.graph.asg.translator.cypher.strategies.expressions;





import com.bpodgursky.jbool_expressions.And;
import com.bpodgursky.jbool_expressions.Expression;
import org.openserach.graph.asg.translator.cypher.strategies.CypherStrategyContext;
import org.openserach.graph.asg.translator.cypher.strategies.CypherUtils;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.quant.QuantBase;

import java.util.List;
import java.util.Optional;

public class AndExpression implements ExpressionStrategies {

    public AndExpression(Iterable<ExpressionStrategies> strategies) {
        this.strategies = strategies;
    }

    @Override
    public void apply(Optional<Expression> parent, Expression expression, AsgQuery query, CypherStrategyContext context) {
        //filter only AND expressions
        //todo parent is empty - create a 'all'-quant as query start
        if (!parent.isPresent()) {
            if(!AsgQueryUtil.nextAdjacentDescendant(query.getStart(), QuantBase.class).isPresent()) {
                CypherUtils.quant(query.getStart().getNext().get(0), Optional.of(expression), query, context);
            }
            context.scope(query.getStart());
        }

        And and = (And) expression;
        CypherUtils.reverse(((List<Expression>) and.getChildren()))
                .forEach(c -> {
                    final AsgEBase<? extends EBase> base = context.getScope();
                    strategies.forEach(s -> {
                        if(s.isApply(c)) s.apply(Optional.of(and), c, query, context);
                    });
                    context.scope(base);
                });
    }

    @Override
    public boolean isApply(Expression expression) {
        return expression instanceof com.bpodgursky.jbool_expressions.And;
    }

    private Iterable<ExpressionStrategies> strategies;

}
