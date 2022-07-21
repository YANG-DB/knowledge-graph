package org.opensearch.graph.epb.plan.pruners;


import org.opensearch.graph.dispatcher.epb.PlanPruneStrategy;
import org.opensearch.graph.dispatcher.utils.PlanUtil;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import javaslang.collection.Stream;

import java.util.*;

public class SymmetricalJoinPruner implements PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> {
    @Override
    public Iterable<PlanWithCost<Plan, PlanDetailedCost>> prunePlans(Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans) {
     return Stream.ofAll(plans).filter(plan -> {
            if(plan.getPlan().getOps().size() == 1) {
                Optional<EntityJoinOp> joinOp = PlanUtil.first(plan.getPlan(), EntityJoinOp.class);
                if(joinOp.isPresent() && joinOp.get().isComplete()){
                    String leftDescription = IterablePlanOpDescriptor.getFull().describe(Collections.singleton(joinOp.get().getLeftBranch()));
                    String rightDescription = IterablePlanOpDescriptor.getFull().describe(Collections.singleton(joinOp.get().getRightBranch()));
                    return leftDescription.compareTo(rightDescription) < 0;
                }
            }
            return true;
        });
    }
}
