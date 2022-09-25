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





import org.opensearch.graph.model.validation.ValidationResult;
import org.opensearch.graph.epb.plan.validation.opValidator.*;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.Plan;

import java.util.Collections;

public class M1PlanValidator extends CompositePlanValidator<Plan, AsgQuery> {

    //region Constructors
    public M1PlanValidator() {
        super(Mode.all);

        this.validators = Collections.singletonList(new ChainedPlanValidator(buildNestedPlanOpValidator(10)));
    }
    //endregion

    //region CompositePlanValidator Implementation
    @Override
    public ValidationResult isPlanValid(Plan plan, AsgQuery query) {
        return super.isPlanValid(plan, query);
    }
    //endregion

    //region Private Methods
    private ChainedPlanValidator.PlanOpValidator buildNestedPlanOpValidator(int numNestingLevels) {
        if (numNestingLevels == 0) {
            return new CompositePlanOpValidator(CompositePlanOpValidator.Mode.all,
//                    new ValidEntityFilterValidator(),
                    new AdjacentPlanOpValidator(),
                    new NoRedundantRelationOpValidator(),
                    new RedundantGoToEntityOpValidator(),
                    new ReverseRelationOpValidator(),
                    new OptionalCompletePlanOpValidator());
        }

        return new CompositePlanOpValidator(CompositePlanOpValidator.Mode.all,
//                new ValidEntityFilterValidator(),
                new AdjacentPlanOpValidator(),
                new NoRedundantRelationOpValidator(),
                new RedundantGoToEntityOpValidator(),
                new ReverseRelationOpValidator(),
                new OptionalCompletePlanOpValidator(),
                new ChainedPlanOpValidator(buildNestedPlanOpValidator(numNestingLevels - 1)));
    }
    //endregion
}
