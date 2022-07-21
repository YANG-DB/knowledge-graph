package org.opensearch.graph.epb.plan.validation.opValidator;



import org.opensearch.graph.epb.plan.validation.ChainedPlanValidator;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.composite.CompositePlanOp;
import org.opensearch.graph.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.validation.ValidationResult;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class SingleEntityJoinValidator implements ChainedPlanValidator.PlanOpValidator {
    @Override
    public void reset() {

    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        if(compositePlanOp.getOps().get(opIndex) instanceof EntityJoinOp && !checkJoin((EntityJoinOp) compositePlanOp.getOps().get(opIndex))){
            return new ValidationResult(false,this.getClass().getSimpleName(), "A complete join cannot have a single entity in one of its branches, " + IterablePlanOpDescriptor.getSimple().describe(compositePlanOp.getOps()));
        }
        return ValidationResult.OK;
    }

    private boolean checkJoin(EntityJoinOp joinOp){
        boolean valid = true;
        List<PlanOp> leftEntities = joinOp.getLeftBranch().getOps().stream().filter(op -> op instanceof EntityOp).collect(toList());
        if(leftEntities.size() == 1 && leftEntities.get(0).getClass().equals(EntityOp.class))
            return false;
        if(joinOp.isComplete()){
            List<PlanOp> rightEntities = joinOp.getRightBranch().getOps().stream().filter(op -> op instanceof EntityOp).collect(toList());
            if(rightEntities.size() == 1 && rightEntities.get(0).getClass().equals(EntityOp.class))
                return false;
        }

        return valid;
    }
}
