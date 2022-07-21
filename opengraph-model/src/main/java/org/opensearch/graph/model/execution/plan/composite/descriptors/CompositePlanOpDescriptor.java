package org.opensearch.graph.model.execution.plan.composite.descriptors;




import com.google.inject.Inject;
import org.opensearch.graph.model.descriptors.Descriptor;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.composite.CompositePlanOp;

/**
 * Created by roman.margolis on 28/11/2017.
 */
public class CompositePlanOpDescriptor implements Descriptor<CompositePlanOp> {
    //region Constructors
    @Inject
    public CompositePlanOpDescriptor(Descriptor<Iterable<PlanOp>> planOpsDescriptor) {
        this.planOpsDescriptor = planOpsDescriptor;
    }
    //endregion

    //region Descriptor Implementation
    @Override
    public String describe(CompositePlanOp compositePlanOp) {
        return new StringBuilder()
                .append(compositePlanOp.getClass().getSimpleName())
                .append("[")
                .append(this.planOpsDescriptor.describe(compositePlanOp.getOps()))
                .append("]")
                .toString();
    }
    //endregion

    //region Fields
    private Descriptor<Iterable<PlanOp>> planOpsDescriptor;
    //endregion
}
