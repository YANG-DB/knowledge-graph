package org.opensearch.graph.dispatcher.validation;







import org.opensearch.graph.model.validation.ValidationResult;

public interface QueryValidator<Q> {
    ValidationResult validate(Q query);
}
