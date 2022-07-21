package org.opensearch.graph.epb.plan.extenders.M2;


import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.epb.plan.extenders.*;
import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.Plan;

import java.util.Optional;

/**
 * Created by Roman on 21/05/2017.
 */
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
