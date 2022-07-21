package org.openserach.graph.asg.translator.cypher.strategies;



import org.opensearch.graph.model.asgQuery.AsgQuery;

public interface CypherTranslatorStrategy {
    void apply(AsgQuery query, CypherStrategyContext context);

}
