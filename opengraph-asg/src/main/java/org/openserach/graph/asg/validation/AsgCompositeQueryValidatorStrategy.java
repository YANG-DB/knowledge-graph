package org.openserach.graph.asg.validation;







import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.validation.ValidationResult;

import java.util.ArrayList;
import java.util.List;

import static org.opensearch.graph.model.asgQuery.AsgCompositeQuery.isComposite;
import static org.opensearch.graph.model.validation.ValidationResult.OK;

public class AsgCompositeQueryValidatorStrategy implements AsgValidatorStrategy {

    public static final String ERROR_1 = "Ontology Composition Error ";

    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();

        if(isComposite(query)) {
            //todo - validate composite query
//            1) check hierarchy level is limited to 1
//            2) check a parameterized constraint exists
//            3) check inner query constraint tag reference is valid

        }

        if (errors.isEmpty())
            return OK;

        return new ValidationResult(false, this.getClass().getSimpleName(), errors.toArray(new String[errors.size()]));
    }
    //endregion
}
