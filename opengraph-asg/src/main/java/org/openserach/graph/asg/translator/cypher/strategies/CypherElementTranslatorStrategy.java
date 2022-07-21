package org.openserach.graph.asg.translator.cypher.strategies;


import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opencypher.v9_0.util.ASTNode;

public interface CypherElementTranslatorStrategy<T extends ASTNode> {
    void apply(T element, AsgQuery query, CypherStrategyContext context);

}
