package org.openserach.graph.asg.validation;







import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.RelPattern;
import org.opensearch.graph.model.validation.ValidationResult;

import java.util.ArrayList;
import java.util.List;

import static org.opensearch.graph.model.asgQuery.AsgQueryUtil.nextDescendants;
import static org.opensearch.graph.model.validation.ValidationResult.OK;

public class AsgRelPatternValidatorStrategy implements AsgValidatorStrategy {

    public static final String ERROR_2 = "Property type mismatch parent Relation ";

    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();
        Ontology.Accessor accessor = context.getOntologyAccessor();
        List<AsgEBase<RelPattern>> list = nextDescendants(query.getStart(), RelPattern.class);

        list.forEach(rel -> {
            //todo implement:
            //      - test range is legal
            //      - test that tag exists it has the eval pattern *$:{}
            //      - test that if a EndPattern exists - only a single exit point from the pattern (RelPattern - ..... - EndPattern)

        });

        if (errors.isEmpty())
            return OK;

        return new ValidationResult(false, this.getClass().getSimpleName(), errors.toArray(new String[errors.size()]));
    }
    //endregion

}
