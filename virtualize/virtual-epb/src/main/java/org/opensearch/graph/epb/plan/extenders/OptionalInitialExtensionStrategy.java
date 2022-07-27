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





import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.dispatcher.utils.PlanUtil;
import org.opensearch.graph.dispatcher.epb.PlanExtensionStrategy;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.OptionalOp;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.entity.EntityNoOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.optional.OptionalComp;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class OptionalInitialExtensionStrategy implements PlanExtensionStrategy<Plan,AsgQuery> {
    //region PlanExtensionStrategy Implementation
    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        if (!plan.isPresent()) {
            return Collections.emptyList();
        }

        Optional<EntityOp> lastEntityOp = PlanUtil.last(plan.get(), EntityOp.class);
        if (!lastEntityOp.isPresent()) {
            return Collections.singletonList(plan.get());
        }

        Optional<EntityOp> previousEntityOp = PlanUtil.prev(plan.get(), lastEntityOp.get(), EntityOp.class);
        if (!previousEntityOp.isPresent()) {
            return Collections.singletonList(plan.get());
        }

        boolean shouldCloseOptional = false;
        List<AsgEBase<? extends EBase>> path = AsgQueryUtil.pathToNextDescendant(previousEntityOp.get().getAsgEbase(), lastEntityOp.get().getAsgEbase().geteNum());
        if (path.isEmpty()) {
            path = AsgQueryUtil.pathToAncestor(previousEntityOp.get().getAsgEbase(), lastEntityOp.get().getAsgEbase().geteNum());
            shouldCloseOptional = true;
        }
        if (path.isEmpty()) {
            //undefined - should not happen
            return Collections.singletonList(plan.get());
        }

        Optional<AsgEBase<OptionalComp>> optionalComp = Stream.ofAll(path)
                .filter(asgEBase -> asgEBase.geteBase() instanceof OptionalComp)
                .map(asgEBase -> (AsgEBase<OptionalComp>)asgEBase)
                .toJavaOptional();

        if (!optionalComp.isPresent()) {
            return Collections.singletonList(plan.get());
        }

        // At this point it is clear that have crossed an optional boundary and should create an OptionalOp
        // The shouldCloseOptional will be true if the boundary was crossed in a reverse direction - meaning
        // that all the plan up to this point should be embedded inside an OptionalOp.
        if (shouldCloseOptional) {
            Plan newPlan = new Plan(new OptionalOp(optionalComp.get(), plan.get().getOps()));
            return Collections.singletonList(newPlan);
        } else {
            // else only the last relation with entity should be included in the OptionalOp
            Optional<RelationOp> relationOp = PlanUtil.prev(plan.get(), lastEntityOp.get(), RelationOp.class);
            if (!relationOp.isPresent()) {
                return Collections.singletonList(plan.get());
            }

            Plan newPlan = plan.get().fromTo(plan.get().getOps().get(0), relationOp.get())
                    .withOp(new OptionalOp(optionalComp.get(),
                            new Plan(new EntityNoOp(previousEntityOp.get().getAsgEbase()))
                                    .append(plan.get().from(relationOp.get())).getOps()));
            return Collections.singletonList(newPlan);
        }
    }
    //endregion
}
