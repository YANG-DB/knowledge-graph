package org.opensearch.graph.asg.strategy.selection;

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






import org.opensearch.graph.asg.strategy.AsgStrategy;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.model.Tagged;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.common.Strings;

import static org.opensearch.graph.model.execution.plan.descriptors.QueryDescriptor.describe;

/**
 * Verify all elements have a tag - if not create one for them
 */
public class DefaultETagAsgStrategy implements AsgStrategy {

    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        AsgQueryUtil.elements(query, element->(element.geteBase() instanceof Tagged)).forEach(e-> {
            if(Strings.isEmpty(((Tagged)e.geteBase()).geteTag())){
                //if no tag exists - create one based on the entity description
                ((Tagged)e.geteBase()).seteTag(describe(e.geteBase()));
            }
        });
    }
    //endregion

}
