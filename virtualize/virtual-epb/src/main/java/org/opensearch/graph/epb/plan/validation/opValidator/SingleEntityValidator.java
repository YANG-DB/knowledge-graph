package org.opensearch.graph.epb.plan.validation.opValidator;


import org.opensearch.graph.epb.plan.validation.ChainedPlanValidator;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.CompositePlanOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.validation.ValidationResult;

/**
 * Validates a single entity op is always accompanied with an EProp
 */
public class SingleEntityValidator implements ChainedPlanValidator.PlanOpValidator {
    @Override
    public void reset() {

    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        if (opIndex == 0) {
            if (!(compositePlanOp.getOps().get(0) instanceof EntityOp)) {
                return new ValidationResult(
                        false,this.getClass().getSimpleName(),
                        "SingleEntity:Validation failed on:" + compositePlanOp.toString() + "<" + opIndex + ">");
            } else if (compositePlanOp.getOps().size() < 2) {
                return new ValidationResult(
                        false,this.getClass().getSimpleName(),
                        "SingleEntity:Validation failed on:" + compositePlanOp.toString() + "<" + opIndex + ">");
            } else {
                try {
                    if (EEntityBase.class.isAssignableFrom(((EntityOp) compositePlanOp.getOps().get(0)).getAsgEbase().geteBase().getClass())) {
                        return ValidationResult.OK;
                    } else
                        return new ValidationResult(
                                false,this.getClass().getSimpleName(),
                                "SingleEntity:Validation failed on:" + compositePlanOp.toString() + "<" + opIndex + ">");
                } catch (ClassCastException cce) {
                    return new ValidationResult(
                            false,this.getClass().getSimpleName(),
                            "SingleEntity:Validation failed on:" + compositePlanOp.toString() + "<" + opIndex + ">");
                }
            }

        }
        return ValidationResult.OK;
    }

}
