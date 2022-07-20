package org.opensearch.graph.epb.plan.extenders.M1;

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

import org.opensearch.graph.dispatcher.epb.PlanExtensionStrategy;
import org.opensearch.graph.epb.plan.extenders.*;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import javaslang.collection.Stream;

/**
 * Created by Roman on 22/05/2017.
 */
public class M1DfsNonRedundantPlanExtensionStrategy extends CompositePlanExtensionStrategy<Plan, AsgQuery> {
    //region Constructors
    public M1DfsNonRedundantPlanExtensionStrategy() {
        super();

        this.innerExtenders = Stream.<PlanExtensionStrategy<Plan, AsgQuery>>of(
                new ChainPlanExtensionStrategy<>(
                        new CompositePlanExtensionStrategy<>(
                                new InitialPlanGeneratorExtensionStrategy(),
                                new StepAdjacentDfsStrategy(),
                                new OptionalOpExtensionStrategy(this)
                        ),
                        new OptionalInitialExtensionStrategy()
                )
        ).toJavaList();
    }
    //endregion
}
