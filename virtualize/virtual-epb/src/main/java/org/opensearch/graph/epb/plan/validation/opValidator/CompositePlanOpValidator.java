package org.opensearch.graph.epb.plan.validation.opValidator;


import org.opensearch.graph.model.validation.ValidationResult;
import org.opensearch.graph.epb.plan.validation.ChainedPlanValidator;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.CompositePlanOp;
import javaslang.collection.Stream;

import java.util.List;

/**
 * Created by Roman on 24/04/2017.
 */
public class CompositePlanOpValidator implements ChainedPlanValidator.PlanOpValidator {
    public enum Mode {
        one,
        all
    }

    //region Constructors
    public CompositePlanOpValidator(Mode mode, ChainedPlanValidator.PlanOpValidator...planOpValidators) {
        this.mode = mode;
        this.planOpValidators = Stream.of(planOpValidators).toJavaList();
    }

    public CompositePlanOpValidator(Mode mode, Iterable<ChainedPlanValidator.PlanOpValidator> planOpValidators) {
        this.mode = mode;
        this.planOpValidators = Stream.ofAll(planOpValidators).toJavaList();
    }
    //endregion

    //region Public Method
    public CompositePlanOpValidator with(ChainedPlanValidator.PlanOpValidator planOpValidator) {
        this.planOpValidators.add(planOpValidator);
        return this;
    }
    //endregion

    //region ChainedPlanValidator.PlanOpValidator Implementation
    @Override
    public void reset() {
        this.planOpValidators.forEach(ChainedPlanValidator.PlanOpValidator::reset);
    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        for(ChainedPlanValidator.PlanOpValidator planOpValidator : this.planOpValidators) {
            ValidationResult planOpValid = planOpValidator.isPlanOpValid(query, compositePlanOp, opIndex);

            if (planOpValid.valid() && this.mode == Mode.one) {
                return ValidationResult.OK;
            }

            if (!planOpValid.valid() && this.mode == Mode.all) {
                return planOpValid;
            }
        }

        if(this.mode == Mode.all) {
            return ValidationResult.OK;
        }

        return new ValidationResult(false,this.getClass().getSimpleName(), "Not all valid");
    }
    //endregion

    //region Fields
    private List<ChainedPlanValidator.PlanOpValidator> planOpValidators;
    private Mode mode;
    //endregion
}
