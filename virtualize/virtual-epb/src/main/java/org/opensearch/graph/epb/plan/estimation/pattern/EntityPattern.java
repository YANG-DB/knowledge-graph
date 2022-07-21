package org.opensearch.graph.epb.plan.estimation.pattern;


import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;

public class EntityPattern extends Pattern {
    //region Constructors
    public EntityPattern(EntityOp start, EntityFilterOp startFilter) {
        this.start = start;
        this.startFilter = startFilter;
    }
    //endregion

    //region Properties
    public EntityOp getStart() {
        return start;
    }

    public EntityFilterOp getStartFilter() {
        return startFilter;
    }
    //endregion

    //region Fields
    private EntityOp start;
    private EntityFilterOp startFilter;

    //endregion
}
