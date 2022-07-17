package org.openserach.graph.asg.strategy.propertyGrouping;

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
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.quant.Quant1;
import org.opensearch.graph.model.query.quant.QuantType;

import java.util.concurrent.atomic.AtomicBoolean;

public class Quant1AllQuantGroupingAsgStrategy implements AsgStrategy {
    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        AtomicBoolean hasWorkToDo = new AtomicBoolean(true);
        while(hasWorkToDo.get()) {
            hasWorkToDo.set(false);

            AsgQueryUtil.elements(query, Quant1.class).forEach(quant -> {
                if (quant.geteBase().getqType().equals(QuantType.all)) {
                    AsgQueryUtil.<Quant1, Quant1>nextAdjacentDescendants(quant, Quant1.class).forEach(childQuant -> {
                        if (childQuant.geteBase().getqType().equals(QuantType.all)) {
                            hasWorkToDo.set(true);
                            AsgQueryUtil.replaceParents(quant,childQuant);
                            AsgQueryUtil.remove(query,childQuant);
                        }
                    });
                }
            });
        }
    }
    //endregion
}
