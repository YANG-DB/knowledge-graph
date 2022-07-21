package org.openserach.graph.asg.translator.sparql.strategies.expressions;


import org.openserach.graph.asg.translator.sparql.strategies.SparqlStrategyContext;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.eclipse.rdf4j.query.algebra.ValueExpr;

public interface ExpressionStrategies {
    void apply(ValueExpr expression,AsgQuery query, SparqlStrategyContext context);

}
