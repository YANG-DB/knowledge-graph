package org.openserach.graph.asg.translator.sparql.strategies;

/*-
 * #%L
 * opengraph-asg
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
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
