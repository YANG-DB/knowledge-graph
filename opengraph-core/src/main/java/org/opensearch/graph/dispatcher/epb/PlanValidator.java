package org.opensearch.graph.dispatcher.epb;





import org.opensearch.graph.model.validation.ValidationResult;

public interface PlanValidator<P, Q>{
    ValidationResult isPlanValid(P plan, Q query);
}
