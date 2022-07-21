package org.opensearch.graph.epb.plan.estimation.pattern;


import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.entity.GoToEntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationFilterOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;

/**
 * Created by Roman on 29/06/2017.
 */
public class GoToEntityRelationEntityPattern extends EntityRelationEntityPattern {
    //region Constructors
    public GoToEntityRelationEntityPattern(GoToEntityOp startGoTo, EntityOp start, EntityFilterOp startFilter, RelationOp rel, RelationFilterOp relFilter, EntityOp end, EntityFilterOp endFilter) {
        super(start, startFilter, rel, relFilter, end, endFilter);
        this.startGoTo = startGoTo;
    }
    //endregion

    //region Properties
    public GoToEntityOp getStartGoTo() {
        return startGoTo;
    }
    //endregion

    //region Fields
    private GoToEntityOp startGoTo;
    //endregion
}
