package org.openserach.graph.asg.translator.cypher.strategies.expressions;


import com.bpodgursky.jbool_expressions.Expression;
import org.openserach.graph.asg.translator.cypher.strategies.CypherElementTranslatorStrategy;
import org.openserach.graph.asg.translator.cypher.strategies.CypherStrategyContext;
import org.openserach.graph.asg.translator.cypher.strategies.CypherUtils;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opencypher.v9_0.ast.Return;

import java.util.Optional;

import static scala.collection.JavaConverters.asJavaCollectionConverter;

public class ReturnClauseNodeCypherTranslator implements CypherElementTranslatorStrategy<Return> {

    public ReturnClauseNodeCypherTranslator(Iterable<ExpressionStrategies> strategies) {
        this.strategies = strategies;
    }

    public void apply(Return aReturn, AsgQuery query, CypherStrategyContext context) {
        asJavaCollectionConverter(aReturn.returnItems().items())
                .asJavaCollection().forEach(
                item -> {
                    Expression c = CypherUtils.reWrite(item.expression());
                    strategies.forEach(s -> {
                        if (s.isApply(c)) s.apply(Optional.empty(), c, query, context);
                    });
                }
        );
    }

    private Iterable<ExpressionStrategies> strategies;
}
