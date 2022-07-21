package org.opensearch.graph.epb.plan.selectors;


import org.opensearch.graph.dispatcher.epb.PlanSelector;
import org.opensearch.graph.epb.plan.extenders.SimpleExtenderUtils;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import javaslang.collection.Stream;

public class CheapestPlanSelector implements PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> {
    @Override
    public Iterable<PlanWithCost<Plan, PlanDetailedCost>> select(AsgQuery query, Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans) {
        return Stream.ofAll(plans).filter(plan -> SimpleExtenderUtils.checkIfPlanIsComplete(plan.getPlan(), query)).minBy((o1, o2) -> {
            if (Double.compare(o1.getCost().getGlobalCost().cost, o2.getCost().getGlobalCost().cost) == 0) {
                return Integer.compare(o1.getPlan().toString().hashCode(), o2.getPlan().toString().hashCode());
            }
            return Double.compare(o1.getCost().getGlobalCost().cost, o2.getCost().getGlobalCost().cost);

        }).toJavaList();
    }
}
