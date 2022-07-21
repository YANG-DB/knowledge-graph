package org.opensearch.graph.epb.plan.estimation;


import org.opensearch.graph.model.execution.plan.PlanWithCost;

import java.util.Optional;

public class IncrementalEstimationContext<P, C, Q> {
    //region Constructors
    public IncrementalEstimationContext(Optional<PlanWithCost<P, C>> previousCost, Q query) {
        this.previousCost = previousCost;
        this.query = query;
    }
    //endregion

    //region Properties
    public Q getQuery() {
        return query;
    }

    public Optional<PlanWithCost<P, C>> getPreviousCost() {
        return previousCost;
    }
    //endregion

    //region Fields
    private Q query;
    private Optional<PlanWithCost<P, C>> previousCost;
    //endregion
}
