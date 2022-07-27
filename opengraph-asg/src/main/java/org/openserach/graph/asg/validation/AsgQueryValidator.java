package org.openserach.graph.asg.validation;

/*-
 * #%L
 * opengraph-asg
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */







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
