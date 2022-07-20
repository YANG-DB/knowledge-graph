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
import org.opensearch.graph.dispatcher.epb.PlanExtensionStrategy;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.optional.OptionalComp;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.quant.Quant1;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Created by moti on 2/27/2017.
 */
public class InitialPlanGeneratorExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {
    public InitialPlanGeneratorExtensionStrategy() {
        this.planPredicate = plan -> true;
    }

    public InitialPlanGeneratorExtensionStrategy(Predicate<Plan> planPredicate) {
        this.planPredicate = planPredicate;
    }

    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        if (plan.isPresent()) {
            return Collections.emptyList();
        }

        List<AsgEBase<EEntityBase>> entitySeeds = AsgQueryUtil.nextDescendants(
                query.getStart(),
                asgEBase -> EEntityBase.class.isAssignableFrom(asgEBase.geteBase().getClass()),
                asgEBase -> !OptionalComp.class.isAssignableFrom(asgEBase.geteBase().getClass()));

        return Stream.ofAll(entitySeeds)
                .map(entitySeed -> {
                    Optional<AsgEBase<Quant1>> entitySeedQuant = AsgQueryUtil.nextAdjacentDescendant(entitySeed, Quant1.class);
                    Optional<AsgEBase<EPropGroup>> epropGroup;
                    if (entitySeedQuant.isPresent()) {
                        epropGroup = AsgQueryUtil.nextAdjacentDescendant(entitySeedQuant.get(), EPropGroup.class);
                    } else {
                        epropGroup = AsgQueryUtil.nextAdjacentDescendant(entitySeed, EPropGroup.class);
                    }

                    Plan newPlan = new Plan(new EntityOp(entitySeed));
                    if (epropGroup.isPresent()) {
                        newPlan = newPlan.withOp(new EntityFilterOp(epropGroup.get()));
                    }

                    return newPlan;
                }).filter(this.planPredicate).toJavaList();
    }

    private Predicate<Plan> planPredicate;

}
