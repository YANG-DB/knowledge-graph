package org.opensearch.graph.epb.plan.extenders.M1;


import com.google.inject.Inject;
import org.opensearch.graph.epb.plan.extenders.*;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.Plan;

import java.util.Optional;

/**
 * Created by Roman on 21/05/2017.
 */
public class M1NonRedundantPlanExtensionStrategy extends CompositePlanExtensionStrategy<Plan, AsgQuery> {
    //region Constructors
    @Inject
    public M1NonRedundantPlanExtensionStrategy() {
        super(
                new CompositePlanExtensionStrategy<>(
                        new InitialPlanGeneratorExtensionStrategy(),
                        //new StepAncestorAdjacentStrategy(),
                        //new StepDescendantsAdjacentStrategy(),
                        new ChainPlanExtensionStrategy<>(
                                new GotoExtensionStrategy(true),
                                new CompositePlanExtensionStrategy<>(
                                        new StepAncestorAdjacentStrategy(),
                                        new StepDescendantsAdjacentStrategy()
                                )
                        )
                )
        );
    }
    //endregion

    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        return super.extendPlan(plan, query);
    }
}
