package org.opensearch.graph.dispatcher.epb;




import org.opensearch.graph.model.validation.ValidationResult;

/**
 * Created by moti on 2/21/2017.
 */
public interface PlanValidator<P, Q>{
    ValidationResult isPlanValid(P plan, Q query);
}
