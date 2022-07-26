package org.openserach.graph.asg.strategy;






import org.openserach.graph.asg.translator.sparql.strategies.SparqlTranslatorStrategy;

public interface SparqlAsgStrategyRegistrar {
    Iterable<SparqlTranslatorStrategy> register();
}
