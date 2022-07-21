package org.openserach.graph.asg.validation;





import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.validation.ValidationResult;

import java.util.ArrayList;
import java.util.List;

import static org.opensearch.graph.model.asgQuery.AsgQueryUtil.elements;
import static org.opensearch.graph.model.validation.ValidationResult.OK;

public class AsgCycleValidatorStrategy implements AsgValidatorStrategy {

    public static final String ERROR_1 = "Ontology Contains Cycle ";

    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();
        List<AsgEBase<EBase>> elements = elements(query);
        if(new java.util.HashSet<>(elements).size() < elements.size())
            errors.add(ERROR_1);


        if (errors.isEmpty())
            return OK;

        return new ValidationResult(false, this.getClass().getSimpleName(), errors.toArray(new String[errors.size()]));
    }
    //endregion
}
