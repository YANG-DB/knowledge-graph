package org.openserach.graph.asg.translator.sparql.strategies;





import org.opensearch.graph.model.asgQuery.AsgQuery;

public interface SparqlTranslatorStrategy {
    void apply(AsgQuery query, SparqlStrategyContext context);

}
