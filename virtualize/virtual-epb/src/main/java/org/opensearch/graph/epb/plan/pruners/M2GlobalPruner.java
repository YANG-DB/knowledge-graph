package org.opensearch.graph.epb.plan.pruners;



import org.opensearch.graph.dispatcher.epb.PlanPruneStrategy;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;

import java.util.Arrays;
import java.util.List;

public class M2GlobalPruner extends CompositePruner<PlanWithCost<Plan, PlanDetailedCost>> {
    public M2GlobalPruner() {
        super(pruners());
    }

    private static List<PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>>> pruners() {
        return Arrays.asList(new SymmetricalJoinPruner());
    }
}
