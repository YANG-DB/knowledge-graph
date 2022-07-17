package org.openserach.graph.asg.strategy.selection;

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


import org.openserach.graph.asg.strategy.AsgStrategy;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.model.Tagged;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.execution.plan.descriptors.QueryDescriptor;
import org.opensearch.graph.model.ontology.EntityType;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.OntologyFinalizer;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.entity.EUntyped;
import org.opensearch.graph.model.query.properties.CalculatedEProp;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.projection.IdentityProjection;
import org.opensearch.graph.model.query.quant.QuantType;
import org.opensearch.common.Strings;

import static org.opensearch.graph.model.execution.plan.descriptors.QueryDescriptor.describe;

/**
 *  verify all steps have an eTag identifier - create on if none exist (will be proficient for profiling)
 */
public class DefaultETagAsgStrategy implements AsgStrategy {
    //region Constructors
    public DefaultETagAsgStrategy(OntologyProvider ontologyProvider) {
        this.ontologyProvider = ontologyProvider;
    }
    //endregion

    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Ontology.Accessor ont = new Ontology.Accessor(this.ontologyProvider.get(query.getOnt()).get());
        AsgQueryUtil.elements(query, element->(element.geteBase() instanceof Tagged)).forEach(e-> {
            if(Strings.isEmpty(((Tagged)e.geteBase()).geteTag())){
                //if no tag exists - create one based on the entity description
                ((Tagged)e.geteBase()).seteTag(describe(e.geteBase()));
            }
        });
    }
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    //endregion
}
