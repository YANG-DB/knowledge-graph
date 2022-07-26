package org.openserach.graph.asg.validation;







import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.validation.ValidationResult;

public interface AsgValidatorStrategy {
    ValidationResult apply(AsgQuery query, AsgStrategyContext context);
}
