package org.openserach.graph.asg.translator.sparql.strategies;



import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.eclipse.rdf4j.query.algebra.Join;
import org.eclipse.rdf4j.query.algebra.TupleExpr;

import java.util.List;

public class JoinPatternTranslatorStrategy extends BinaryPatternTranslatorStrategy {

    public JoinPatternTranslatorStrategy(List<SparqlElementTranslatorStrategy> translatorStrategies) {
        super(translatorStrategies);
    }

    @Override
    public void apply(TupleExpr element, AsgQuery query, SparqlStrategyContext context) {
        //combine two statements into a step / FQN (Fully Qualified Node)
        if (Join.class.isAssignableFrom(element.getClass())) {
            //todo - infer join (Conjunction) knowledge
            super.apply(element, query, context);
        }
    }
}
