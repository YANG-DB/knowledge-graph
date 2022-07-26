package org.opensearch.graph.epb.plan.extenders.M1;





import org.opensearch.graph.dispatcher.epb.PlanExtensionStrategy;
import org.opensearch.graph.epb.plan.extenders.*;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import javaslang.collection.Stream;

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
