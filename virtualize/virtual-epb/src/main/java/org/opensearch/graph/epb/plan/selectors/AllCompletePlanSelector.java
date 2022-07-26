package org.opensearch.graph.epb.plan.selectors;





import org.opensearch.graph.dispatcher.epb.PlanSelector;
import org.opensearch.graph.epb.plan.extenders.SimpleExtenderUtils;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.PlanWithCost;

import java.util.ArrayList;
import java.util.List;

public class AllCompletePlanSelector<C> implements PlanSelector<PlanWithCost<Plan, C>, AsgQuery> {
    //region PlanSelector Implementation
    @Override
    public Iterable<PlanWithCost<Plan, C>> select(AsgQuery query, Iterable<PlanWithCost<Plan, C>> plans) {
        List<PlanWithCost<Plan, C>> selectedPlans = new ArrayList<>();
        for(PlanWithCost<Plan, C> planWithCost : plans) {
            if(SimpleExtenderUtils.checkIfPlanIsComplete(planWithCost.getPlan(), query)){
                selectedPlans.add(planWithCost);
            }
        }

        return selectedPlans;
    }
    //endregion
}
