package org.opensearch.graph.epb.plan.estimation.pattern;





import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationFilterOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;

public class EntityRelationEntityPattern extends Pattern {
    //region Constructors
    public EntityRelationEntityPattern(
            EntityOp start,
            EntityFilterOp startFilter,
            RelationOp rel,
            RelationFilterOp relFilter,
            EntityOp end,
            EntityFilterOp endFilter) {
        this.start = start;
        this.startFilter = startFilter;
        this.rel = rel;
        this.relFilter = relFilter;
        this.end = end;
        this.endFilter = endFilter;
    }
    //endregion

    //region Properties
    public EntityOp getStart() {
        return start;
    }

    public EntityFilterOp getStartFilter() {
        return startFilter;
    }

    public RelationOp getRel() {
        return rel;
    }

    public RelationFilterOp getRelFilter() {
        return relFilter;
    }

    public EntityOp getEnd() {
        return end;
    }

    public EntityFilterOp getEndFilter() {
        return endFilter;
    }
    //endregion

    //region Fields
    private EntityOp start;
    private EntityFilterOp startFilter;
    private RelationOp rel;
    private RelationFilterOp relFilter;
    private EntityOp end;
    private EntityFilterOp endFilter;
    //endregion
}
