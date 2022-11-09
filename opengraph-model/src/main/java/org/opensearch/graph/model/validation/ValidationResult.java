package org.opensearch.graph.model.validation;

/*-
 * #%L
 * opengraph-model
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


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import javaslang.collection.Stream;
import org.opensearch.graph.model.Printable;

import java.util.*;

/**
 * Created by lior.perry on 5/29/2017.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationResult {
    public static ValidationResult OK = new ValidationResult(true, "none");

    public static String print(Object... elements) {
        StringJoiner joiner = new StringJoiner(":", "[", "]");
        for (Object element : elements) {
            if (Printable.class.isAssignableFrom(element.getClass()))
                ((Printable) element).print(joiner);
            else
                joiner.add(element.toString());
        }
        return joiner.toString();
    }

    //region Constructors

    public ValidationResult() {
    }

    public ValidationResult(boolean valid, String validator, String... errors) {
        this(valid, validator, Stream.of(errors));
    }

    public ValidationResult(boolean valid, String validator, Iterable<String> errors) {
        this.valid = valid;
        this.validator = validator;
        this.errors = Stream.ofAll(errors).toJavaList();
    }
    //endregion

    //region Public Methods
    public boolean valid() {
        return valid;
    }

    public Iterable<String> errors() {
        return errors;
    }
    //endregion

    //region Override Methods
    @Override
    public String toString() {
        if (valid())
            return "valid";
        return print(errors + ":" + validator);
    }

    public String getValidator() {
        return validator;
    }
//endregion

    //region Fields
    @JsonProperty("validator")
    private String validator;
    @JsonProperty("isValid")
    private boolean valid;
    @JsonProperty("errors")
    private Iterable<String> errors;
    //endregion

    public static class ValidationResults {
        private List<ValidationResult> validations = new ArrayList<>();

        public ValidationResults() {
        }

        public ValidationResults( List<ValidationResult> validations) {
            this.validations = validations;
        }

        public boolean isValid() {
            return getValidations().isEmpty();
        }

        public List<ValidationResult> getValidations() {
            return validations;
        }

        public ValidationResults with(ValidationResult validation) {
            this.validations.add(validation);
            return this;
        }
    }
}
