package org.openserach.graph.asg.translator.sparql.strategies;


import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.eclipse.rdf4j.query.algebra.*;

import java.util.List;

/**
 * Arbitraty path (determine min-max hopes) query element
 * Todo - implement
 */
public class ArbitraryPathTranslatorStrategy extends UnaryPatternTranslatorStrategy{

    public ArbitraryPathTranslatorStrategy(List<SparqlElementTranslatorStrategy> translatorStrategies) {
        super(translatorStrategies);
    }

    @Override
    public void apply(TupleExpr element, AsgQuery query, SparqlStrategyContext context) {
        if(ArbitraryLengthPath.class.isAssignableFrom(element.getClass())) {
            //subject
            Var subjectVar = ((ArbitraryLengthPath) element).getSubjectVar();
            //arbitrary path expression
            super.apply(((ArbitraryLengthPath) element).getPathExpression(), query, context);
            //object
            Var objectVar = ((ArbitraryLengthPath) element).getObjectVar();
        }
    }
}
