package org.opensearch.graph.epb.plan.extenders;



import org.opensearch.graph.dispatcher.epb.PlanExtensionStrategy;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.Optional;

public class JoinSeedExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {
    private PlanExtensionStrategy<Plan, AsgQuery> seedStrategy;

    public JoinSeedExtensionStrategy(PlanExtensionStrategy<Plan, AsgQuery> seedStrategy) {
        this.seedStrategy = seedStrategy;
    }

    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        // Cannot create a new join from an empty plan
        if (!plan.isPresent() || plan.get().getOps().isEmpty()) {
            return Collections.emptyList();
        }

        return Stream.ofAll(seedStrategy.extendPlan(Optional.empty(), query))
                .map(seedRightBranch -> new Plan(new EntityJoinOp(plan.get(), seedRightBranch)))
                .toJavaList();
    }
}
