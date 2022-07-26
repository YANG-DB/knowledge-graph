package org.opensearch.graph.epb.plan.validation.opValidator;





import org.opensearch.graph.model.validation.ValidationResult;
import org.opensearch.graph.epb.plan.validation.ChainedPlanValidator;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.composite.CompositePlanOp;

public class ChainedPlanOpValidator implements ChainedPlanValidator.PlanOpValidator {
    //region Constructors
    public ChainedPlanOpValidator(ChainedPlanValidator.PlanOpValidator planOpValidator) {
        this.planOpValidator = planOpValidator;
    }
    //endregion

    //region PlanOpValidator Implementation
    @Override
    public void reset() {

    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        PlanOp currentPlanOp = compositePlanOp.getOps().get(opIndex);
        if (!CompositePlanOp.class.isAssignableFrom(currentPlanOp.getClass())) {
            return ValidationResult.OK;
        }

        CompositePlanOp currentCompositePlanOp = (CompositePlanOp)currentPlanOp;
        this.planOpValidator.reset();
        for (int innerOpIndex = 0 ; innerOpIndex < currentCompositePlanOp.getOps().size() ; innerOpIndex++) {
            ValidationResult valid = planOpValidator.isPlanOpValid(query, currentCompositePlanOp, innerOpIndex);
            if(!valid.valid()) {
                return valid;
            }
        }

        return ValidationResult.OK;
    }
    //endregion

    //region Fields
    private ChainedPlanValidator.PlanOpValidator planOpValidator;
    //endregion
}
