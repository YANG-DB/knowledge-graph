package org.opensearch.graph.asg.validation;

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
