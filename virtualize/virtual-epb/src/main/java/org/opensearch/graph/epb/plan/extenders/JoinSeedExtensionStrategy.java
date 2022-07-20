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

import org.opensearch.graph.dispatcher.epb.PlanExtensionStrategy;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.Optional;

/**
 * Created by moti on 7/3/2017.
 * Generated new Join ops, places the old plan as the left branch of the join, and creates seeds
 * for the right branch (with a seed strategy)
 */
public class JoinSeedExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {
    private PlanExtensionStrategy<Plan, AsgQuery> seedStrategy;

    public JoinSeedExtensionStrategy(PlanExtensionStrategy<Plan, AsgQuery> seedStrategy) {
        this.seedStrategy = seedStrategy;
    }

    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        // Cannot create a new join from an empty plan
        if (!plan.isPresent() || plan.get().getOps().isEmpty()) {
            return Collections.emptyList();
        }

        return Stream.ofAll(seedStrategy.extendPlan(Optional.empty(), query))
                .map(seedRightBranch -> new Plan(new EntityJoinOp(plan.get(), seedRightBranch)))
                .toJavaList();
    }
}
