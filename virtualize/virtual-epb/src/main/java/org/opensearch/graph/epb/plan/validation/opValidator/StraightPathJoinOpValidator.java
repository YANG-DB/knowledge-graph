package org.opensearch.graph.epb.plan.validation.opValidator;

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
import org.opensearch.graph.epb.plan.validation.ChainedPlanValidator;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.composite.CompositePlanOp;
import org.opensearch.graph.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.validation.ValidationResult;

import java.util.*;

public class StraightPathJoinOpValidator implements ChainedPlanValidator.PlanOpValidator {
    private boolean isPathAvailable(EntityJoinOp entityJoinOp, AsgQuery query) {
        Set<Integer> leftEntities = getEntityOpsRecursively(entityJoinOp.getLeftBranch().getOps(),new HashSet<>());
        EntityOp lastRightEntity = PlanUtil.last(entityJoinOp.getRightBranch(), EntityOp.class).get();
        Set<Integer> visitedNodes = new HashSet<>();
        Queue<Integer> seeds = new LinkedList<>();
        seeds.add(lastRightEntity.getAsgEbase().geteNum());
        while(!seeds.isEmpty()){
            Integer currentEntity = seeds.poll();
            if(currentEntity == entityJoinOp.getAsgEbase().geteNum())
                return true;
            visitedNodes.add(currentEntity);


            AsgEBase<EBase> currentElement = AsgQueryUtil.element$(query, currentEntity);


            List<AsgEBase<EBase>> descendants = AsgQueryUtil.nextDescendantsSingleHop(currentElement, EEntityBase.class);
            for (AsgEBase<EBase> descendant : descendants) {
                if(entityJoinOp.getAsgEbase().geteNum() == descendant.geteNum())
                    return true;
                if(!leftEntities.contains(descendant.geteNum()) && !visitedNodes.contains(descendant.geteNum()))
                    seeds.add(descendant.geteNum());
            }

            Optional<AsgEBase<EBase>> ancestor = AsgQueryUtil.ancestor(currentElement, EEntityBase.class);
            if(ancestor.isPresent()) {
                if (entityJoinOp.getAsgEbase().geteNum() == ancestor.get().geteNum())
                    return true;
                if (!leftEntities.contains(ancestor.get().geteNum()) && !visitedNodes.contains(ancestor.get().geteNum()))
                    seeds.add(ancestor.get().geteNum());
            }

        }

        return false;
    }

    private Set<Integer> getEntityOpsRecursively(List<PlanOp> ops, Set<Integer> set) {
        for (PlanOp op : ops) {
            if (op instanceof EntityOp) {
                set.add(((EntityOp) op).getAsgEbase().geteNum());
            }
            if (op instanceof EntityJoinOp) {
                getEntityOpsRecursively(((EntityJoinOp) op).getLeftBranch().getOps(), set);
                getEntityOpsRecursively(((EntityJoinOp) op).getRightBranch().getOps(), set);
            }
        }
        return set;
    }

    @Override
    public void reset() {

    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        PlanOp planOp = compositePlanOp.getOps().get(opIndex);
        if(planOp instanceof EntityJoinOp){
            EntityJoinOp join = (EntityJoinOp) planOp;
            if(opIndex == 0 && compositePlanOp.getOps().size() == 1 && !isPathAvailable(join, query)){
                return new ValidationResult(false,this.getClass().getSimpleName(), "JoinOp path validation failed: " + IterablePlanOpDescriptor.getSimple().describe(compositePlanOp.getOps()));
            }
        }
        return ValidationResult.OK;
    }
}
