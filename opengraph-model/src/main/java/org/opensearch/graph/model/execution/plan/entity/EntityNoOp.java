package org.opensearch.graph.model.execution.plan.entity;






import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.query.entity.EEntityBase;

/**
 * Created by roman.margolis on 27/11/2017.
 */
public class EntityNoOp extends EntityOp {
    //region Constructors
    public EntityNoOp() {
        super(new AsgEBase<>());
    }

    public EntityNoOp(AsgEBase<EEntityBase> entity) {
        super(entity);
    }
    //endregion
}
