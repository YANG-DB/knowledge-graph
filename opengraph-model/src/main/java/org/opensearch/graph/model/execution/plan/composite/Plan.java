package org.opensearch.graph.model.execution.plan.composite;






import org.opensearch.graph.model.execution.plan.IPlan;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor;

/**
 * Created by lior.perry on 22/02/2017.
 */
public class Plan extends CompositePlanOp implements IPlan {
    //region Constructors
    public Plan() {}

    public Plan(PlanOp... ops) {
        super(ops);
    }

    public Plan(Iterable<PlanOp> ops) {
        super(ops);
    }
    //endregion

    public static boolean contains(Plan plan, PlanOp op) {
        return plan.getOps().stream().anyMatch(p->p.equals(op));
    }

    public static boolean equals(Plan plan, Plan newPlan) {
        return IterablePlanOpDescriptor.getSimple().describe(newPlan.getOps())
                .compareTo(IterablePlanOpDescriptor.getSimple().describe(plan.getOps())) == 0;
    }

    public static boolean equals(Plan plan, Plan newPlan,IterablePlanOpDescriptor descriptor) {
        return descriptor.describe(newPlan.getOps())
                .compareTo(descriptor.describe(plan.getOps())) == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return equals((Plan)o,this);
    }

    @Override
    public int hashCode() {
        return IterablePlanOpDescriptor.getSimple().describe(this.getOps()).hashCode();
    }

    public static Plan clone(Plan plan) {
        return new Plan(plan.getOps());
    }
}
