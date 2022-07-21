package org.openserach.graph.asg.translator.cypher;




import org.openserach.graph.asg.translator.cypher.strategies.CypherTranslatorStrategy;

/**
 * Created by lior.perry on 27/02/2017.
 */
public interface CypherAsgStrategyRegistrar {
    Iterable<CypherTranslatorStrategy> register();
}
