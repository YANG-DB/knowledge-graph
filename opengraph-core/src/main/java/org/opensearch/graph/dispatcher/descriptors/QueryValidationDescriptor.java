package org.opensearch.graph.dispatcher.descriptors;




import org.opensearch.graph.model.validation.ValidationResult;
import org.opensearch.graph.model.descriptors.Descriptor;

/**
 * Created by roman.margolis on 29/11/2017.
 */
public class QueryValidationDescriptor implements Descriptor<ValidationResult> {
    //region Descriptor Implementation
    @Override
    public String describe(ValidationResult context) {
        return String.valueOf(context.valid());
    }
    //endregion
}
