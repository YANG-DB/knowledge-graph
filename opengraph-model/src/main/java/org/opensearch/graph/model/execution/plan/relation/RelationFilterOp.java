package org.opensearch.graph.model.execution.plan.relation;






import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.execution.plan.AsgEBasePlanOp;
import org.opensearch.graph.model.execution.plan.Filter;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.properties.RelPropGroup;

/**
 * Created by lior.perry on 22/02/2017.
 */
public class RelationFilterOp extends AsgEBasePlanOp<RelPropGroup> implements Filter {
    //region Constructors
    public RelationFilterOp() {
        super(new AsgEBase<>());
    }

    public RelationFilterOp(AsgEBase<RelPropGroup> relPropGroup) {
        super(relPropGroup);
    }
    //endregion

    //region Properties
    public AsgEBase<Rel> getRel() {
        return rel;
    }

    public void setRel(AsgEBase<Rel> rel) {
        this.rel = rel;
    }
    //endregion

    //region Fields
    private AsgEBase<Rel> rel;
    //endregion
}
