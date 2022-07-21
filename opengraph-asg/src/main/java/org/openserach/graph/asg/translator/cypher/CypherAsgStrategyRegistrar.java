package org.openserach.graph.asg.translator.cypher;





import org.openserach.graph.asg.translator.cypher.strategies.CypherTranslatorStrategy;

public interface CypherAsgStrategyRegistrar {
    Iterable<CypherTranslatorStrategy> register();
}
