package org.opensearch.graph.model.execution.plan.entity;




import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.execution.plan.AsgEBasePlanOp;
import org.opensearch.graph.model.query.entity.EEntityBase;

/**
 * Created by lior.perry on 20/02/2017.
 */
public class EntityOp extends AsgEBasePlanOp<EEntityBase> {
    //region Constructor
    public EntityOp() {
        super(new AsgEBase<>());
    }

    public EntityOp(AsgEBase<EEntityBase> asgEBase) {
        super(asgEBase);
    }

    @Override
    public AsgEBase<EEntityBase> getAsgEbase() {
        return super.getAsgEbase();
    }

    //endregion
}
