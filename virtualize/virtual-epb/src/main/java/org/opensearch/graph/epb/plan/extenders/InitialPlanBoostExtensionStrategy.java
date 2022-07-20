package org.opensearch.graph.epb.plan.extenders;

/*-
 * #%L
 * virtual-epb
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
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.RankingProp;
import javaslang.collection.Stream;

import java.util.Optional;

public class InitialPlanBoostExtensionStrategy extends InitialPlanGeneratorExtensionStrategy {

    public InitialPlanBoostExtensionStrategy() {
        super(plan -> {
            if(plan.getOps().size() > 0){
                PlanOp last = Stream.ofAll(plan.getOps()).last();
                if(EntityFilterOp.class.isAssignableFrom(last.getClass())){
                    EntityFilterOp entityFilterOp = (EntityFilterOp) last;
                    return ePropContainsBoost(entityFilterOp.getAsgEbase().geteBase());
                }
            }
            return false;
        });
    }

    private static boolean ePropContainsBoost(EPropGroup ePropGroup) {
        if(ePropGroup instanceof RankingProp) {
            return true;
        }
        if(!Stream.ofAll(ePropGroup.getGroups()).find(g -> g instanceof RankingProp).isEmpty()){
            return true;
        }
        if(!Stream.ofAll(ePropGroup.getProps()).find(p -> p instanceof RankingProp).isEmpty()){
            return true;
        }

        return !Stream.ofAll(ePropGroup.getGroups()).find(g -> ePropContainsBoost(g)).isEmpty();
    }

    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        return Stream.ofAll(super.extendPlan(plan, query)).take(1).toJavaList();
    }
}
