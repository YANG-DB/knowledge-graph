package org.opensearch.graph.asg.translator.sparql.strategies;

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





import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.query.EBase;
import org.eclipse.rdf4j.query.algebra.Projection;
import org.eclipse.rdf4j.query.algebra.ProjectionElemList;
import org.eclipse.rdf4j.query.algebra.TupleExpr;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectionPatternTranslatorStrategy extends UnaryPatternTranslatorStrategy{

    public ProjectionPatternTranslatorStrategy(List<SparqlElementTranslatorStrategy> translatorStrategies) {
        super(translatorStrategies);
    }

    @Override
    public void apply(TupleExpr element, AsgQuery query, SparqlStrategyContext context) {
        if(Projection.class.isAssignableFrom(element.getClass())) {
            //collect projection names - and match them to the tagged elements within the actual query
            ProjectionElemList list = ((Projection) element).getProjectionElemList();
            Map<String, List<AsgEBase<EBase>>> tags = AsgQueryUtil.groupByTags(query.getStart());
            //remove not present tags in the projection
            list.getTargetNames().stream()
                    .filter(e->!tags.containsKey(e)).collect(Collectors.toList())
                    .forEach(rmv->tags.remove(rmv));
            //populate the query
            query.setProjectedFields(tags);
        }
        super.apply(element, query, context);
    }
}
