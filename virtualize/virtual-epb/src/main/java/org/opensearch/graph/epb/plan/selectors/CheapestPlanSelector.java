package org.opensearch.graph.epb.plan.selectors;

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





import org.opensearch.graph.dispatcher.epb.PlanSelector;
import org.opensearch.graph.epb.plan.extenders.SimpleExtenderUtils;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import javaslang.collection.Stream;

public class CheapestPlanSelector implements PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> {
    @Override
    public Iterable<PlanWithCost<Plan, PlanDetailedCost>> select(AsgQuery query, Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans) {
        return Stream.ofAll(plans).filter(plan -> SimpleExtenderUtils.checkIfPlanIsComplete(plan.getPlan(), query)).minBy((o1, o2) -> {
            if (Double.compare(o1.getCost().getGlobalCost().cost, o2.getCost().getGlobalCost().cost) == 0) {
                return Integer.compare(o1.getPlan().toString().hashCode(), o2.getPlan().toString().hashCode());
            }
            return Double.compare(o1.getCost().getGlobalCost().cost, o2.getCost().getGlobalCost().cost);

        }).toJavaList();
    }
}
