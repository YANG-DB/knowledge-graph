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
import org.opensearch.graph.model.execution.plan.*;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationFilterOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.RelPropGroup;
import org.opensearch.graph.model.query.quant.Quant1;

import java.util.Collections;
import java.util.Optional;

import static org.opensearch.graph.epb.plan.extenders.SimpleExtenderUtils.getNextAncestorUnmarkedOfType;

public class StepAncestorAdjacentStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {

    //region PlanExtensionStrategy Implementation
    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        if (!plan.isPresent()) {
            return Collections.emptyList();
        }

        Optional<AsgEBase<Rel>> nextRelation = SimpleExtenderUtils.getNextAncestorOfType(plan.get(), Rel.class);
        if (!nextRelation.isPresent()) {
            return Collections.emptyList();
        }

        Optional<AsgEBase<RelPropGroup>> nextRelationPropGroup = AsgQueryUtil.bDescendant(nextRelation.get(), RelPropGroup.class);

        Optional<AsgEBase<EEntityBase>> toEntity = AsgQueryUtil.ancestor(nextRelation.get(), EEntityBase.class);
        if (!toEntity.isPresent()) {
            return Collections.emptyList();
        }

        Optional<AsgEBase<Quant1>> toEntityQuant = AsgQueryUtil.nextAdjacentDescendant(toEntity.get(), Quant1.class);
        Optional<AsgEBase<EPropGroup>> toEntityPropGroup = toEntityQuant.isPresent() ?
                AsgQueryUtil.nextAdjacentDescendant(toEntityQuant.get(), EPropGroup.class) :
                AsgQueryUtil.nextAdjacentDescendant(toEntity.get(), EPropGroup.class);


        Plan newPlan = Plan.clone(plan.get());
        //current pattern on plan is the "getFrom" entity whether is entity or filter op
        RelationOp relationOp = new RelationOp(nextRelation.get(), Direction.reverse(nextRelation.get().geteBase().getDir()));
        newPlan = newPlan.withOp(relationOp);
        if (nextRelationPropGroup.isPresent()) {
            newPlan = newPlan.withOp(new RelationFilterOp(nextRelationPropGroup.get()));
        }
        //getTo entity pattern
        newPlan = newPlan.withOp(new EntityOp(toEntity.get()));
        if (toEntityPropGroup.isPresent()) {
            newPlan = newPlan.withOp(new EntityFilterOp(toEntityPropGroup.get()));
        }

        return Collections.singletonList(newPlan);
    }
    //endregion

}
