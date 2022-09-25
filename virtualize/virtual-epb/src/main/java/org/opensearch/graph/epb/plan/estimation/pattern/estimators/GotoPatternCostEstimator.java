package org.opensearch.graph.epb.plan.estimation.pattern.estimators;

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





import org.opensearch.graph.epb.plan.estimation.IncrementalEstimationContext;
import org.opensearch.graph.epb.plan.estimation.pattern.GotoPattern;
import org.opensearch.graph.epb.plan.estimation.pattern.Pattern;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.CountEstimatesCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;

public class GotoPatternCostEstimator implements PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> {
    @Override
    public Result<Plan, CountEstimatesCost> estimate(Pattern pattern, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery> context) {
        GotoPattern gotoPattern = (GotoPattern) pattern;

        PlanWithCost<Plan, CountEstimatesCost> entityOpCost = context.getPreviousCost().get().getCost().getPlanStepCost(gotoPattern.getEntityOp()).get();
        CountEstimatesCost gotoCost = new CountEstimatesCost(0, entityOpCost.getCost().peek());

        return Result.of(new double[]{1}, new PlanWithCost<>(new Plan(gotoPattern.getGoToEntityOp()), gotoCost));
    }
}
