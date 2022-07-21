package org.opensearch.graph.model.execution.plan.entity;




import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.query.entity.EEntityBase;

/**
 * Created by Roman on 30/04/2017.
 */
public class GoToEntityOp extends EntityOp {
    //region Constructors
    public GoToEntityOp() {
        super(new AsgEBase<>());
    }

    public GoToEntityOp(AsgEBase<EEntityBase> entity) {
        super(entity);
    }
    //endregion
}
