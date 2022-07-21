package org.opensearch.graph.epb.plan.validation.opValidator;



import org.opensearch.graph.dispatcher.epb.PlanValidator;
import org.opensearch.graph.epb.plan.validation.ChainedPlanValidator;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.composite.CompositePlanOp;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import org.opensearch.graph.model.validation.ValidationResult;
import javaslang.collection.Stream;

public class JoinOpCompositeValidator implements ChainedPlanValidator.PlanOpValidator{
    private PlanValidator<Plan, AsgQuery> leftValidator;
    private PlanValidator<Plan, AsgQuery> rightValidator;

    public JoinOpCompositeValidator(PlanValidator<Plan, AsgQuery> leftValidator, PlanValidator<Plan, AsgQuery> rightValidator) {
        this.leftValidator = leftValidator;
        this.rightValidator = rightValidator;
    }

    @Override
    public void reset() {

    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        PlanOp planOp = compositePlanOp.getOps().get(opIndex);
        if(planOp instanceof EntityJoinOp){
            EntityJoinOp joinOp = (EntityJoinOp) planOp;
            ValidationResult leftValidationContext = this.leftValidator.isPlanValid(joinOp.getLeftBranch(), query);
            ValidationResult rightValidationContext = this.rightValidator.isPlanValid(joinOp.getRightBranch(), query);
            return new ValidationResult(leftValidationContext.valid() && rightValidationContext.valid(),
                    this.getClass().getSimpleName(),
                    Stream.ofAll(leftValidationContext.errors()).appendAll(rightValidationContext.errors()));

        }
        return ValidationResult.OK;
    }
}
