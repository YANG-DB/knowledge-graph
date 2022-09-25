package org.opensearch.graph.epb.plan.extenders.M2;

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





import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.epb.plan.extenders.*;
import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.Plan;

import java.util.Optional;

public class M2PlanExtensionStrategy extends CompositePlanExtensionStrategy<Plan, AsgQuery> {
    //region Constructors
    @Inject
    public M2PlanExtensionStrategy(
            OntologyProvider ontologyProvider,
            GraphElementSchemaProviderFactory schemaProviderFactory) {
        super(
                new ChainPlanExtensionStrategy<>(
                        new CompositePlanExtensionStrategy<>(
                                new InitialPlanGeneratorExtensionStrategy(),
                                new JoinSeedExtensionStrategy(new InitialPlanGeneratorExtensionStrategy()),
                                new JoinOngoingExtensionStrategy(
                                        getJoinInnerExpander(2)),
                                new StepAncestorAdjacentStrategy(),
                                new StepDescendantsAdjacentStrategy(),
                                new ChainPlanExtensionStrategy<>(
                                        new CompositePlanExtensionStrategy<>(//new GotoExtensionStrategy(),
                                                new GotoJoinExtensionStrategy()),
                                        new CompositePlanExtensionStrategy<>(
                                                new StepAncestorAdjacentStrategy(),
                                                new StepDescendantsAdjacentStrategy()
                                        )
                                )
                        ),
                        new RedundantFilterPlanExtensionStrategy(
                                ontologyProvider,
                                schemaProviderFactory)
                )
        );
    }

    private static ChainPlanExtensionStrategy<Plan, AsgQuery> getJoinInnerExpander(int depth) {
        if(depth == 0) {
            return new ChainPlanExtensionStrategy<>(
                    new CompositePlanExtensionStrategy<>(
                            new InitialPlanGeneratorExtensionStrategy(),
                            new StepAncestorAdjacentStrategy(),
                            new StepDescendantsAdjacentStrategy(),
                            new ChainPlanExtensionStrategy<>(
                                    new CompositePlanExtensionStrategy<>(//new GotoExtensionStrategy(),
                                            new GotoJoinExtensionStrategy()),
                                    new CompositePlanExtensionStrategy<>(
                                            new StepAncestorAdjacentStrategy(),
                                            new StepDescendantsAdjacentStrategy()
                                    )
                            )
                    ));
        }

        return new ChainPlanExtensionStrategy<>(
                new CompositePlanExtensionStrategy<>(
                        new InitialPlanGeneratorExtensionStrategy(),
                        new StepAncestorAdjacentStrategy(),
                        new StepDescendantsAdjacentStrategy(),
                        new ChainPlanExtensionStrategy<>(
                                new CompositePlanExtensionStrategy<>(//new GotoExtensionStrategy(),
                                        new GotoJoinExtensionStrategy()),
                                new CompositePlanExtensionStrategy<>(
                                        new StepAncestorAdjacentStrategy(),
                                        new StepDescendantsAdjacentStrategy()
                                )
                        ),
                        new JoinOngoingExtensionStrategy(
                                getJoinInnerExpander(depth-1)),
                        new JoinSeedExtensionStrategy(new InitialPlanGeneratorExtensionStrategy())
                ));
    }
    //endregion

    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        return super.extendPlan(plan, query);
    }
}
