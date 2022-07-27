package org.opensearch.graph.epb.plan.pruners;

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





import org.opensearch.graph.dispatcher.epb.PlanPruneStrategy;
import org.opensearch.graph.model.execution.plan.PlanWithCost;

import java.util.Collections;
import java.util.Iterator;

public class FirstPlanPruneStrategy<P, C> implements PlanPruneStrategy<PlanWithCost<P, C>> {
    //region PlanPruneStrategy Implementation
    @Override
    public Iterable<PlanWithCost<P, C>> prunePlans(Iterable<PlanWithCost<P, C>> plans) {
        Iterator<PlanWithCost<P, C>> planIterator = plans.iterator();
        return planIterator.hasNext() ? Collections.singletonList(plans.iterator().next()) : Collections.emptyList();
    }
    //endegion
}
