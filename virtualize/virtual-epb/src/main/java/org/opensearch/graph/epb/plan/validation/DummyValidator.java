package org.opensearch.graph.epb.plan.validation;


import org.opensearch.graph.model.validation.ValidationResult;
import org.opensearch.graph.dispatcher.epb.PlanValidator;

/**
 * Created by moti on 2/23/2017.
 */
public class DummyValidator<P,Q> implements PlanValidator<P,Q> {
    @Override
    public ValidationResult isPlanValid(P plan, Q query) {
        return ValidationResult.OK;
    }
}
