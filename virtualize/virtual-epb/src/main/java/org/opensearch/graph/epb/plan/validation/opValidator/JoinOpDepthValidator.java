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

import org.opensearch.graph.epb.plan.validation.ChainedPlanValidator;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.CompositePlanOp;
import org.opensearch.graph.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import org.opensearch.graph.model.validation.ValidationResult;

/**
 * Validates join ops by checking nesting levels
 */
public class JoinOpDepthValidator implements ChainedPlanValidator.PlanOpValidator {
    public JoinOpDepthValidator(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public JoinOpDepthValidator() {
        this(3);
    }

    @Override
    public void reset() {

    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        if(compositePlanOp.getOps().get(opIndex) instanceof EntityJoinOp){
            if(maxDepth <= 0){
                return new ValidationResult(false,this.getClass().getSimpleName(), "Too many nested joins , " + IterablePlanOpDescriptor.getSimple().describe(compositePlanOp.getOps()));
            }
        }
        return ValidationResult.OK;
    }
    private int maxDepth;
}
