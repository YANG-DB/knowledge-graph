package org.opensearch.graph.epb.plan.validation.opValidator;



import org.opensearch.graph.dispatcher.utils.PlanUtil;
import org.opensearch.graph.epb.plan.validation.ChainedPlanValidator;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.composite.CompositePlanOp;
import org.opensearch.graph.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.log.Trace;
import org.opensearch.graph.model.validation.ValidationResult;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class JoinIntersectionPlanOpValidator implements ChainedPlanValidator.PlanOpValidator {
    private Trace<String> trace = Trace.build(JoinIntersectionPlanOpValidator.class.getSimpleName());


    //region Private Methods
    private boolean isIntersectionValid(EntityJoinOp joinOp) {
        Optional<EntityOp> leftLast = PlanUtil.last(joinOp.getLeftBranch(), EntityOp.class);
        Optional<EntityOp> rightLast = PlanUtil.last(joinOp.getRightBranch(), EntityOp.class);
        Set<Integer> leftEopSet = getEntityOpsRecursively(joinOp.getLeftBranch().getOps(), new HashSet<>());
        Set<Integer> rightEopSet = getEntityOpsRecursively(joinOp.getRightBranch().getOps(), new HashSet<>());

        Set<Integer> intersection = new HashSet<>(leftEopSet);
        intersection.retainAll(rightEopSet);

        //0 intersection is OK, since we can be in a state where we didn't finish yet.
        if (intersection.size() == 0) {
            return true;
        }
        if (intersection.size() == 1) {
            return leftLast.isPresent() &&
                    rightLast.isPresent() &&
                    leftLast.get().getAsgEbase().geteNum() == rightLast.get().getAsgEbase().geteNum() &&
                    !joinOp.getLeftBranch().equals(joinOp.getRightBranch());
        }

        return false;
    }

    private Set<Integer> getEntityOpsRecursively(List<PlanOp> ops, Set<Integer> set) {
        for (PlanOp op : ops) {
            if (op instanceof EntityOp) {
                set.add(((EntityOp)op).getAsgEbase().geteNum());
            }
            if (op instanceof EntityJoinOp) {
                getEntityOpsRecursively(((EntityJoinOp) op).getLeftBranch().getOps(), set);
                getEntityOpsRecursively(((EntityJoinOp) op).getRightBranch().getOps(), set);
            }
        }
        return set;
    }

    @Override
    public void reset() {

    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        PlanOp planOp = compositePlanOp.getOps().get(opIndex);
        if(planOp instanceof EntityJoinOp) {
            EntityJoinOp joinOp = (EntityJoinOp) planOp;
            if (compositePlanOp.getOps().size() == 1 && !isIntersectionValid(joinOp)) {
                return new ValidationResult(false,this.getClass().getSimpleName(), "JoinOp intersection validation failed: " + IterablePlanOpDescriptor.getSimple().describe(compositePlanOp.getOps()));
            }
        }
        return ValidationResult.OK;
    }

    //endregion

}
