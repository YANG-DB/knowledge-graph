package org.opensearch.graph.epb.plan.pruners;

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





import org.opensearch.graph.dispatcher.epb.PlanPruneStrategy;
import org.opensearch.graph.dispatcher.utils.PlanUtil;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import javaslang.collection.Stream;

import java.util.*;

public class SymmetricalJoinPruner implements PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> {
    @Override
    public Iterable<PlanWithCost<Plan, PlanDetailedCost>> prunePlans(Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans) {
     return Stream.ofAll(plans).filter(plan -> {
            if(plan.getPlan().getOps().size() == 1) {
                Optional<EntityJoinOp> joinOp = PlanUtil.first(plan.getPlan(), EntityJoinOp.class);
                if(joinOp.isPresent() && joinOp.get().isComplete()){
                    String leftDescription = IterablePlanOpDescriptor.getFull().describe(Collections.singleton(joinOp.get().getLeftBranch()));
                    String rightDescription = IterablePlanOpDescriptor.getFull().describe(Collections.singleton(joinOp.get().getRightBranch()));
                    return leftDescription.compareTo(rightDescription) < 0;
                }
            }
            return true;
        });
    }
}
