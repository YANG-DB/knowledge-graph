package org.openserach.graph.asg.strategy;



import org.openserach.graph.asg.translator.sparql.strategies.SparqlTranslatorStrategy;

/**
 * Created by lior.perry on 27/02/2017.
 */
public interface SparqlAsgStrategyRegistrar {
    Iterable<SparqlTranslatorStrategy> register();
}
