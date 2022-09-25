package org.opensearch.graph.epb.plan.estimation;

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





import org.opensearch.graph.model.execution.plan.PlanWithCost;

import java.util.Optional;

public class IncrementalEstimationContext<P, C, Q> {
    //region Constructors
    public IncrementalEstimationContext(Optional<PlanWithCost<P, C>> previousCost, Q query) {
        this.previousCost = previousCost;
        this.query = query;
    }
    //endregion

    //region Properties
    public Q getQuery() {
        return query;
    }

    public Optional<PlanWithCost<P, C>> getPreviousCost() {
        return previousCost;
    }
    //endregion

    //region Fields
    private Q query;
    private Optional<PlanWithCost<P, C>> previousCost;
    //endregion
}
