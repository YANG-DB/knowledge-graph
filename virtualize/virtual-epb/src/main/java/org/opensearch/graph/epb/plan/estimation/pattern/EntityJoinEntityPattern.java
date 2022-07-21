package org.opensearch.graph.epb.plan.estimation.pattern;



import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationFilterOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;

public class EntityJoinEntityPattern extends EntityRelationEntityPattern {
    public EntityJoinEntityPattern(EntityJoinOp entityJoinOp, RelationOp rel, RelationFilterOp relFilter, EntityOp end, EntityFilterOp endFilter) {
        super(entityJoinOp, null, rel, relFilter, end, endFilter);
        this.entityJoinOp = entityJoinOp;
    }

    public EntityJoinOp getEntityJoinOp() {
        return entityJoinOp;
    }

    private EntityJoinOp entityJoinOp;
}
