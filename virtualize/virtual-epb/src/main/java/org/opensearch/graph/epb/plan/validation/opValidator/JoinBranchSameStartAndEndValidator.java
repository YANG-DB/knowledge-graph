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





import org.opensearch.graph.dispatcher.utils.PlanUtil;
import org.opensearch.graph.epb.plan.validation.ChainedPlanValidator;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.CompositePlanOp;
import org.opensearch.graph.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.validation.ValidationResult;

public class JoinBranchSameStartAndEndValidator implements ChainedPlanValidator.PlanOpValidator {

    @Override
    public void reset() {

    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        if(compositePlanOp.getOps().get(opIndex) instanceof EntityJoinOp && !checkJoin((EntityJoinOp) compositePlanOp.getOps().get(opIndex))){
            return new ValidationResult(false,this.getClass().getSimpleName(), "A join branch cannot start and end in the same entity, " + IterablePlanOpDescriptor.getSimple().describe(compositePlanOp.getOps()));
        }
        return ValidationResult.OK;
    }

    private boolean checkJoin(EntityJoinOp joinOp){
        boolean valid = true;
        EntityOp firstLeftEntity = PlanUtil.first$(joinOp.getLeftBranch(), EntityOp.class);
        if(firstLeftEntity.getAsgEbase().geteBase().equals(joinOp.getAsgEbase().geteBase()) && !(firstLeftEntity instanceof EntityJoinOp))
            valid = false;

        EntityOp firstRightEntity = PlanUtil.first(joinOp.getRightBranch(), EntityOp.class).get();
        if(firstRightEntity.getAsgEbase().geteBase().equals(joinOp.getAsgEbase().geteBase()) && !(firstRightEntity instanceof EntityJoinOp))
            valid = false;


        return valid;
    }
}
