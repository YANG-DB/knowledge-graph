package org.openserach.graph.asg.translator.sparql.strategies;





import org.openserach.graph.asg.translator.sparql.strategies.expressions.ExpressionStrategies;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.eclipse.rdf4j.query.algebra.Filter;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.ValueExpr;

import java.util.List;

public class FilterPatternTranslatorStrategy extends UnaryPatternTranslatorStrategy{

    private final List<ExpressionStrategies> whereExpressionStrategies;

    public FilterPatternTranslatorStrategy(List<SparqlElementTranslatorStrategy> translatorStrategies, List<ExpressionStrategies> whereExpressionStrategies) {
        super(translatorStrategies);
        this.whereExpressionStrategies = whereExpressionStrategies;
    }

    @Override
    public void apply(TupleExpr element, AsgQuery query, SparqlStrategyContext context) {
        super.apply(element, query, context);
        //only after building the query structure elements - match the predicate filter elements
        if(Filter.class.isAssignableFrom(element.getClass())) {
            //collect condition
            ValueExpr condition = ((Filter) element).getCondition();
            //apply where strategies
            whereExpressionStrategies.stream().forEach(st->st.apply(condition,query,context));
        }
    }
}
