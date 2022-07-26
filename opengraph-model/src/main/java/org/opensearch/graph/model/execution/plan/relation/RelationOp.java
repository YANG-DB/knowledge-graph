package org.opensearch.graph.model.execution.plan.relation;






import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.execution.plan.AsgEBasePlanOp;
import org.opensearch.graph.model.query.Rel;

/**
 * Created by lior.perry on 20/02/2017.
 */
public class RelationOp extends AsgEBasePlanOp<Rel> {
    //region Constructors
    public RelationOp() {
        super(new AsgEBase<>());
    }

    public RelationOp(AsgEBase<Rel> relation) {
        super(relation);
    }

    public RelationOp(AsgEBase<Rel> relation, Rel.Direction direction) {
        super(new AsgEBase<>(relation.geteBase().clone()));
        getAsgEbase().geteBase().setDir(direction);
    }
    //endregion
}
