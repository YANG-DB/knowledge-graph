package org.opensearch.graph.epb.plan.validation;



import org.opensearch.graph.model.validation.ValidationResult;
import org.opensearch.graph.dispatcher.epb.PlanValidator;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.CompositePlanOp;
import org.opensearch.graph.model.execution.plan.composite.Plan;

public class ChainedPlanValidator implements PlanValidator<Plan, AsgQuery> {

    public interface PlanOpValidator {
        void reset();
        ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex);
    }

    //region Constructors
    public ChainedPlanValidator(PlanOpValidator planOpValidator) {
        this.planOpValidator = planOpValidator;
    }
    //endregion

    //region PlanValidator Implementation
    @Override
    public ValidationResult isPlanValid(Plan plan, AsgQuery query) {
        this.planOpValidator.reset();

        for (int opIndex = 0 ; opIndex < plan.getOps().size() ; opIndex++) {
            ValidationResult valid = planOpValidator.isPlanOpValid(query, plan, opIndex);
            if(!valid.valid()) {
                return valid;
            }
        }

        return ValidationResult.OK;
    }
    //endregion

    //region Fields
    private PlanOpValidator planOpValidator;
    //endregion
}


