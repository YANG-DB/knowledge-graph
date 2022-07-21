package org.opensearch.graph.dispatcher.validation;




import org.opensearch.graph.model.validation.ValidationResult;

/**
 * Created by Roman on 12/15/2017.
 */
public interface QueryValidator<Q> {
    ValidationResult validate(Q query);
}
