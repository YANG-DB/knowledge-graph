package org.opensearch.graph.model.execution.plan.relation;




import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.execution.plan.AsgEBasePlanOp;
import org.opensearch.graph.model.query.aggregation.AggBase;

/**
 * Created by lior.perry on 22/02/2017.
 */
public class RelationGroupingOp extends AsgEBasePlanOp<AggBase> {
    //region Constructors
    public RelationGroupingOp() {
        super(new AsgEBase<>());
    }

    public RelationGroupingOp(AsgEBase<AggBase> agg) {
        super(agg);
    }
    //endregion
}
