package org.opensearch.graph.model.execution.plan.composite.descriptors;




import org.opensearch.graph.model.descriptors.Descriptor;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.composite.UnionOp;

public class UnionOpDescriptor implements Descriptor<UnionOp> {
    public UnionOpDescriptor(Descriptor<Iterable<PlanOp>> planOpsDescriptor) {
        this.planOpsDescriptor = planOpsDescriptor;
    }

    @Override
    public String describe(UnionOp item) {
        final StringBuilder builder = new StringBuilder()
                .append(item.getClass().getSimpleName())
                .append("(")
                .append(item)
                .append(")");
        item.getPlans().forEach(p ->
                builder.append("[")
                        .append(this.planOpsDescriptor.describe(p.getOps()))
                        .append("]"));
        return builder.toString();
    }

    private Descriptor<Iterable<PlanOp>> planOpsDescriptor;
}
