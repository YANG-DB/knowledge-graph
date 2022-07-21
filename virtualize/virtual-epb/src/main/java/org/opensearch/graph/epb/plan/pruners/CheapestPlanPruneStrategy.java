package org.opensearch.graph.epb.plan.pruners;



import org.opensearch.graph.dispatcher.epb.PlanPruneStrategy;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import javaslang.collection.Stream;

public class CheapestPlanPruneStrategy implements PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> {

    @Override
    public Iterable<PlanWithCost<Plan, PlanDetailedCost>> prunePlans(Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans) {
        return Stream.ofAll(plans).minBy((o1, o2) -> {
            if (Double.compare(o1.getCost().getGlobalCost().cost, o2.getCost().getGlobalCost().cost) == 0) {
                return Integer.compare(o1.getPlan().toString().hashCode(), o2.getPlan().toString().hashCode());
            }
            return Double.compare(o1.getCost().getGlobalCost().cost, o2.getCost().getGlobalCost().cost);

        }).toJavaList();
    }
}
