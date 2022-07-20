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
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.dispatcher.utils.PlanUtil;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.entity.GoToEntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationFilterOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.RelPropGroup;
import org.opensearch.graph.model.query.quant.Quant1;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.Optional;

import static org.opensearch.graph.epb.plan.extenders.SimpleExtenderUtils.getNextDescendantUnmarkedOfType;

/**
 * Created by Roman on 23/04/2017.
 */
public class StepAdjacentDfsStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {
    //region PlanExtensionStrategy Implementation
    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        if (!plan.isPresent()) {
            return Collections.emptyList();
        }
        //get next relation which was not already visited
        Optional<AsgEBase<Rel>> nextRelation = getNextDescendantUnmarkedOfType(plan.get(), Rel.class);
        if (!nextRelation.isPresent()) {
            return Collections.emptyList();
        }

        RelationOp relationOp = new RelationOp(nextRelation.get());
        Optional<AsgEBase<RelPropGroup>> nextRelationPropGroup = AsgQueryUtil.bDescendant(nextRelation.get(), RelPropGroup.class);

        //try ancestor first
        Optional<AsgEBase<EEntityBase>> fromEntity = AsgQueryUtil.ancestor(nextRelation.get(), EEntityBase.class);
        Optional<AsgEBase<EEntityBase>> toEntity = AsgQueryUtil.nextDescendant(nextRelation.get(), EEntityBase.class);
        //if ancestor not part of the plan try descendant
        if (!PlanUtil.contains(plan.get(), fromEntity.get().geteNum())) {
            fromEntity = AsgQueryUtil.nextDescendant(nextRelation.get(), EEntityBase.class);
            toEntity = AsgQueryUtil.ancestor(nextRelation.get(), EEntityBase.class);
            relationOp = new RelationOp(AsgQueryUtil.reverse(nextRelation.get()));
        }


        Optional<AsgEBase<Quant1>> toEntityQuant = AsgQueryUtil.nextAdjacentDescendant(toEntity.get(), Quant1.class);
        Optional<AsgEBase<EPropGroup>> toEntityPropGroup;
        if (toEntityQuant.isPresent()) {
            toEntityPropGroup = AsgQueryUtil.nextAdjacentDescendant(toEntityQuant.get(), EPropGroup.class);
        } else {
            toEntityPropGroup = AsgQueryUtil.nextAdjacentDescendant(toEntity.get(), EPropGroup.class);
        }

        Plan newPlan = plan.get();

        PlanOp lastPlanOp = plan.get().getOps().get(plan.get().getOps().size() - 1);
        if (PlanUtil.last$(newPlan, EntityOp.class).getAsgEbase().geteNum() != fromEntity.get().geteNum() ||
                Stream.of(EntityOp.class, EntityFilterOp.class).filter(klazz -> klazz.isAssignableFrom(lastPlanOp.getClass())).isEmpty()) {
            newPlan = newPlan.withOp(new GoToEntityOp(fromEntity.get()));
        }

        newPlan = newPlan.withOp(relationOp);
        if (nextRelationPropGroup.isPresent()) {
            newPlan = newPlan.withOp(new RelationFilterOp(nextRelationPropGroup.get()));
        }

        newPlan = newPlan.withOp(new EntityOp(toEntity.get()));
        if (toEntityPropGroup.isPresent()) {
            newPlan = newPlan.withOp(new EntityFilterOp(toEntityPropGroup.get()));
        }

        return Collections.singletonList(newPlan);
    }
    //endregion

}
