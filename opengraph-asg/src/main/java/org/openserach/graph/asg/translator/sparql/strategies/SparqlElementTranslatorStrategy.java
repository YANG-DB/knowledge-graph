package org.openserach.graph.asg.translator.sparql.strategies;


import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.eclipse.rdf4j.query.algebra.TupleExpr;

public interface SparqlElementTranslatorStrategy<T extends TupleExpr> {
    void apply(T element, AsgQuery query, SparqlStrategyContext context);

}
