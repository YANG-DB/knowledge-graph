package org.openserach.graph.asg.translator.sparql.strategies;


import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.eclipse.rdf4j.query.algebra.BinaryTupleOperator;
import org.eclipse.rdf4j.query.algebra.TupleExpr;

import java.util.List;

public abstract class BinaryPatternTranslatorStrategy implements SparqlElementTranslatorStrategy {
    private List<SparqlElementTranslatorStrategy> translatorStrategies;

    public BinaryPatternTranslatorStrategy(List<SparqlElementTranslatorStrategy> translatorStrategies) {
        this.translatorStrategies = translatorStrategies;
    }

    @Override
    public void apply(TupleExpr element, AsgQuery query, SparqlStrategyContext context) {
        if (BinaryTupleOperator.class.isAssignableFrom(element.getClass())) {
            translatorStrategies.forEach(st -> st.apply(((BinaryTupleOperator) element).getLeftArg(), query, context));
            translatorStrategies.forEach(st -> st.apply(((BinaryTupleOperator) element).getRightArg(), query, context));
        }
    }
}
