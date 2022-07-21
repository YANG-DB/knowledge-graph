package org.opensearch.graph.model.execution.plan.composite.descriptors;




import org.opensearch.graph.model.descriptors.Descriptor;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;

public class EntityJoinOpDescriptor implements Descriptor<EntityJoinOp> {
    public EntityJoinOpDescriptor(Descriptor<Iterable<PlanOp>> planOpsDescriptor) {
        this.planOpsDescriptor = planOpsDescriptor;
    }

    @Override
    public String describe(EntityJoinOp item) {
        return new StringBuilder()
                .append(item.getClass().getSimpleName())
                .append("(")
                .append(item)
                .append(")")
                .append("[left:[")
                .append(this.planOpsDescriptor.describe(item.getLeftBranch().getOps()))
                .append("], right:[")
                .append(this.planOpsDescriptor.describe(item.getRightBranch().getOps()))
                .append("]]")
                .toString();
    }

    private Descriptor<Iterable<PlanOp>> planOpsDescriptor;
}
