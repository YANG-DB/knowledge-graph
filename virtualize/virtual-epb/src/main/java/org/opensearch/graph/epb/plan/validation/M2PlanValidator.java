package org.opensearch.graph.epb.plan.validation;

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

import org.opensearch.graph.epb.plan.validation.opValidator.*;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.validation.ValidationResult;

import java.util.Arrays;

/**
 *
 */
public class M2PlanValidator extends CompositePlanValidator<Plan,AsgQuery> {

    //region Constructors
    public M2PlanValidator() {
        super(Mode.all);

        //this.validators = Collections.singletonList(new ChainedPlanValidator(buildNestedPlanOpValidator(10)));
        this.validators = Arrays.asList(new ChainedPlanValidator(buildNestedPlanOpValidator(10,3 , false)));
    }
    //endregion

    //region CompositePlanValidator Implementation
    @Override
    public ValidationResult isPlanValid(Plan plan, AsgQuery query) {
        return super.isPlanValid(plan, query);
    }
    //endregion

    //region Private Methods
    private ChainedPlanValidator.PlanOpValidator buildNestedPlanOpValidator(int numNestingLevels, int joinDepth, boolean validateJoinOnly) {
        if (numNestingLevels == 0) {
                    return new CompositePlanOpValidator(CompositePlanOpValidator.Mode.all,
//                            new SingleEntityValidator(),
                            new AdjacentPlanOpValidator(),
                            new NoRedundantRelationOpValidator(),
                            new RedundantGoToEntityOpValidator(),
                            new ReverseRelationOpValidator(),
                            new OptionalCompletePlanOpValidator(),
                            new JoinCompletePlanOpValidator(),
                            new JoinIntersectionPlanOpValidator(),
                            new JoinOpDepthValidator(joinDepth),
                            new SingleEntityJoinValidator(),
                            new JoinBranchSameStartAndEndValidator(),
                            new StraightPathJoinOpValidator());
        }
        ChainedPlanValidator.PlanOpValidator leftJoinBranchValidator = buildNestedPlanOpValidator(numNestingLevels - 1, joinDepth-1, true);
        ChainedPlanValidator.PlanOpValidator rightJoinBranchValidator = buildNestedPlanOpValidator(numNestingLevels - 1, joinDepth-1, validateJoinOnly);
        return new CompositePlanOpValidator(CompositePlanOpValidator.Mode.all,
//                new SingleEntityValidator(),
                new AdjacentPlanOpValidator(),
                new NoRedundantRelationOpValidator(),
                new RedundantGoToEntityOpValidator(),
                new ReverseRelationOpValidator(),
                new OptionalCompletePlanOpValidator(),
                new JoinCompletePlanOpValidator(validateJoinOnly),
                new JoinIntersectionPlanOpValidator(),
                new JoinOpDepthValidator(joinDepth),
                new StraightPathJoinOpValidator(),
                new SingleEntityJoinValidator(),
                new JoinBranchSameStartAndEndValidator(),
                new JoinOpCompositeValidator(
                        new ChainedPlanValidator(leftJoinBranchValidator),
                        new ChainedPlanValidator(rightJoinBranchValidator)),
                new ChainedPlanOpValidator(leftJoinBranchValidator)
                );
    }
    //endregion
}
