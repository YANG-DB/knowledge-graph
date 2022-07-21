package org.openserach.graph.asg.translator.sparql.strategies;


import org.openserach.graph.asg.translator.sparql.strategies.expressions.ExpressionStrategies;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

import java.util.List;


public class RootTranslatorStrategy implements SparqlTranslatorStrategy{
    private final List<SparqlElementTranslatorStrategy> translatorStrategies;
    private final List<ExpressionStrategies> whereExpressionStrategies;

    public RootTranslatorStrategy(List<SparqlElementTranslatorStrategy> translatorStrategies, List<ExpressionStrategies> whereExpressionStrategies) {
        this.translatorStrategies = translatorStrategies;
        this.whereExpressionStrategies = whereExpressionStrategies;
    }

    @Override
    public void apply(AsgQuery query, SparqlStrategyContext context) {
        ParsedQuery statement = context.getStatement();
        Dataset dataset = statement.getDataset();
        //root parsed query entry level
        TupleExpr expr = statement.getTupleExpr();
        //structure query elements
        translatorStrategies.forEach(strategy->strategy.apply(expr,query,context));
    }


}
