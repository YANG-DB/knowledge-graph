package org.opensearch.graph.epb.plan.estimation.pattern;



import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.entity.GoToEntityOp;

public class GotoPattern extends Pattern {

    public GotoPattern(GoToEntityOp goToEntityOp, EntityOp entityOp) {
        this.goToEntityOp = goToEntityOp;
        this.entityOp = entityOp;
    }

    public GoToEntityOp getGoToEntityOp() {
        return goToEntityOp;
    }

    public EntityOp getEntityOp() {
        return entityOp;
    }

    private GoToEntityOp goToEntityOp;
    private EntityOp entityOp;

}
