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





import org.opensearch.graph.model.validation.ValidationResult;
import org.opensearch.graph.epb.plan.validation.ChainedPlanValidator;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.*;
import org.opensearch.graph.model.execution.plan.composite.CompositePlanOp;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.entity.GoToEntityOp;

import java.util.HashSet;
import java.util.Set;

public class RedundantGoToEntityOpValidator implements ChainedPlanValidator.PlanOpValidator {
    //region Constructors
    public RedundantGoToEntityOpValidator() {
        this.entityEnums = new HashSet<>();
    }
    //endregion

    //region ChainedPlanValidator.PlanOpValidator Implementation
    @Override
    public void reset() {
        this.entityEnums.clear();
    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        PlanOp planOp = compositePlanOp.getOps().get(opIndex);
        if (planOp instanceof GoToEntityOp) {
            if (!this.entityEnums.contains(((AsgEBaseContainer)planOp).getAsgEbase().geteNum())) {
                return new ValidationResult(
                        false,this.getClass().getSimpleName(),
                        "GoTo:Validation failed on:" +   compositePlanOp.toString() + "<" + opIndex + ">");
            }
        }

        if (planOp instanceof EntityOp) {
            this.entityEnums.add(((AsgEBaseContainer)planOp).getAsgEbase().geteNum());
        }



        if(planOp instanceof EntityJoinOp){
            recursiveEntityNums((EntityJoinOp) planOp);
        }
        return ValidationResult.OK;

    }
    //endregion

    private void recursiveEntityNums(EntityJoinOp joinOp){
        joinOp.getLeftBranch().getOps().forEach(op -> {
            if(op instanceof EntityOp){
                this.entityEnums.add(((AsgEBaseContainer)op).getAsgEbase().geteNum());
            }
            if(op instanceof EntityJoinOp){
                recursiveEntityNums((EntityJoinOp) op);
            }
        });

        joinOp.getRightBranch().getOps().forEach(op -> {
            if(op instanceof EntityOp){
                this.entityEnums.add(((AsgEBaseContainer)op).getAsgEbase().geteNum());
            }
            if(op instanceof EntityJoinOp){
                recursiveEntityNums((EntityJoinOp) op);
            }
        });

    }

    //region Fields
    private Set<Integer> entityEnums;
    //endregion
}
