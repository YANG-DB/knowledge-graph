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





import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.UnaryTupleOperator;

import java.util.List;

public abstract class UnaryPatternTranslatorStrategy implements SparqlElementTranslatorStrategy{
    private List<SparqlElementTranslatorStrategy> translatorStrategies;

    public UnaryPatternTranslatorStrategy(List<SparqlElementTranslatorStrategy> translatorStrategies) {
        this.translatorStrategies = translatorStrategies;
    }

    @Override
    public void apply(TupleExpr element, AsgQuery query, SparqlStrategyContext context) {
        if(UnaryTupleOperator.class.isAssignableFrom(element.getClass())) {
            //collect projection names
            TupleExpr arg = ((UnaryTupleOperator) element).getArg();
            translatorStrategies.forEach(st->st.apply(arg,query,context));
        }
    }
}
