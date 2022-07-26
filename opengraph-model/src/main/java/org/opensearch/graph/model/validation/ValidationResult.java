package org.opensearch.graph.model.validation;






import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import javaslang.collection.Stream;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * Created by lior.perry on 5/29/2017.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationResult {
    public static ValidationResult OK = new ValidationResult(true, "none");

    public static String print(Object... elements) {
        StringJoiner joiner = new StringJoiner(":", "[", "]");
        Arrays.asList(elements).forEach(element -> joiner.add(element.toString()));
        return joiner.toString();
    }

    //region Constructors

    public ValidationResult() {}

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
}
