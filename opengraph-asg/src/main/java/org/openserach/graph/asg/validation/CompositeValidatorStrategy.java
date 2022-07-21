package org.openserach.graph.asg.validation;




import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.validation.ValidationResult;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.List;

public class CompositeValidatorStrategy implements AsgValidatorStrategy {
    //region Constructors
    public CompositeValidatorStrategy(AsgValidatorStrategy...strategies) {
        this(Stream.of(strategies));
    }

    public CompositeValidatorStrategy(Iterable<AsgValidatorStrategy> strategies) {
        this.strategies = Stream.ofAll(strategies).toJavaList();
    }
    //endregion

    //region AsgValidatorStrategy Implementation
    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        List<ValidationResult> contexts = new ArrayList<>();
        for(AsgValidatorStrategy strategy : this.strategies) {
            try {
                contexts.add(strategy.apply(query, context));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        List<String> errors = Stream.ofAll(contexts)
                .filter(validationContext -> !validationContext.valid())
                .flatMap(validationContext -> Stream.ofAll(validationContext.errors()))
                .toJavaList();

        return new ValidationResult(errors.isEmpty(), this.getClass().getSimpleName(), errors);
    }
    //endregion

    //region Fields
    private Iterable<AsgValidatorStrategy> strategies;
    //endregion
}
