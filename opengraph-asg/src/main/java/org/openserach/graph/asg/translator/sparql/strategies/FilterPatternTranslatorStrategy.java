package org.openserach.graph.asg.translator.sparql.strategies;

/*-
 * #%L
 * opengraph-asg
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */





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
