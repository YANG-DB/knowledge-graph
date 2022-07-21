package org.openserach.graph.asg.validation;





import java.util.Collections;

public class AsgValidatorStrategyRegistrarImpl implements AsgValidatorStrategyRegistrar {
    //region AsgStrategyRegistrar Implementation
    @Override
    public Iterable<AsgValidatorStrategy> register() {
        return Collections.singletonList(new CompositeValidatorStrategy(
                new AsgConstraintExpressionValidatorStrategy(),
                new AsgCycleValidatorStrategy(),
                new AsgCompositeQueryValidatorStrategy(),
                new AsgEntityDuplicateEnumValidatorStrategy(),
                new AsgEntityDuplicateETagValidatorStrategy(),
                new AsgEntityPropertiesValidatorStrategy(),
                new AsgOntologyEntityValidatorStrategy(),
                new AsgOntologyRelValidatorStrategy(),
                new AsgRelPropertiesValidatorStrategy(),
                new AsgStartEntityValidatorStrategy(),
                new AsgWhereByConstraintValidatorStrategy(),
                new AsgStepsValidatorStrategy()
        ));
    }
    //endregion
}
