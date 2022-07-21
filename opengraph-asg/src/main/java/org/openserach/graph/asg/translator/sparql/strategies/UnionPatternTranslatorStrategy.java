package org.openserach.graph.asg.translator.sparql.strategies;


import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.query.EBase;
import org.eclipse.rdf4j.query.algebra.*;

import java.util.List;

public class UnionPatternTranslatorStrategy extends BinaryPatternTranslatorStrategy {

    public UnionPatternTranslatorStrategy(List<SparqlElementTranslatorStrategy> translatorStrategies) {
        super(translatorStrategies);
    }

    @Override
    public void apply(TupleExpr element, AsgQuery query, SparqlStrategyContext context) {
        //combine two statements into a step / FQN (Fully Qualified Node)
        if (Union.class.isAssignableFrom(element.getClass())) {
            //todo - infer union knowledge
            super.apply(element,query,context);
        }
    }
}
