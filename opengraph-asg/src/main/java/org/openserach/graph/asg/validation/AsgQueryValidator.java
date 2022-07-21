package org.openserach.graph.asg.validation;




import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.dispatcher.validation.QueryValidator;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.validation.ValidationResult;
import javaslang.collection.Stream;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AsgQueryValidator implements QueryValidator<AsgQuery> {
    //region Constructors
    @Inject
    public AsgQueryValidator(
            AsgValidatorStrategyRegistrar asgValidatorStrategyRegistrar,
            OntologyProvider ontologyProvider) {
        this.asgValidatorStrategies = asgValidatorStrategyRegistrar.register();
        this.ontologyProvider = ontologyProvider;
    }
    //endregion

    //region QueryValidator Implementation
    @Override
    public ValidationResult validate(AsgQuery query) {
        Optional<Ontology> ontology = this.ontologyProvider.get(query.getOnt());
        if (!ontology.isPresent()) {
            return new ValidationResult(false,this.getClass().getSimpleName(), "unknown ontology");
        }

        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(new Ontology.Accessor(ontology.get()));

        List<ValidationResult> validationResults = Stream.ofAll(this.asgValidatorStrategies)
                .map(strategy -> strategy.apply(query, asgStrategyContext))
                .toJavaList();

        List<String> errors = Stream.ofAll(validationResults)
                .filter(queryValidation -> !queryValidation.valid())
                .flatMap(queryValidation -> Stream.ofAll(queryValidation.errors()))
                .toJavaList();

        final String validators = validationResults.stream()
                .filter(queryValidation -> !queryValidation.valid())
                .map(ValidationResult::getValidator)
                .collect(Collectors.joining(","));

        return errors.isEmpty() ?
                ValidationResult.OK :
                new ValidationResult(false, validators, errors);
    }
    //endregion

    //region Fields
    private Iterable<AsgValidatorStrategy> asgValidatorStrategies;
    private OntologyProvider ontologyProvider;
    //endregion
}
