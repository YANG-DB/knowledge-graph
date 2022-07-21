package org.opensearch.graph.model.execution.plan.entity;




import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.execution.plan.AsgEBasePlanOp;
import org.opensearch.graph.model.execution.plan.Filter;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.properties.EPropGroup;

/**
 * Created by lior.perry on 20/02/2017.
 */
public class EntityFilterOp extends AsgEBasePlanOp<EPropGroup> implements Filter {
    //region Constructors
    public EntityFilterOp() {
        super(new AsgEBase<>());
    }

    public EntityFilterOp(AsgEBase<EPropGroup> asgEBase) {
        super(asgEBase);
    }

    public EntityFilterOp(AsgEBase<EPropGroup> asgEBase, AsgEBase<EEntityBase> entity) {
        super(asgEBase);
        this.entity = entity;
    }
    //endregion

    //region Properties
    public AsgEBase<EEntityBase> getEntity() {
        return entity;
    }

    public void setEntity(AsgEBase<EEntityBase> entity) {
        this.entity = entity;
    }
    //endregion

    //region Fields
    private AsgEBase<EEntityBase> entity;
    //endregion
}
