package org.opensearch.graph.epb.plan.selectors;


import org.opensearch.graph.dispatcher.epb.PlanSelector;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.PlanWithCost;

public class SelectAllPlanSelector<C> implements PlanSelector<PlanWithCost<Plan, C>, AsgQuery> {
    //region PlanSelector Implementation
    @Override
    public Iterable<PlanWithCost<Plan, C>> select(AsgQuery query, Iterable<PlanWithCost<Plan, C>> plans) {
        return plans;
    }
    //endregion
}
