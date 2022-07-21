package org.openserach.graph.asg.validation;





import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.Start;
import org.opensearch.graph.model.validation.ValidationResult;

import java.util.List;

import static org.opensearch.graph.model.asgQuery.AsgQueryUtil.elements;
import static org.opensearch.graph.model.asgQuery.AsgQueryUtil.nextDescendants;
import static org.opensearch.graph.model.validation.ValidationResult.OK;

public class AsgStartEntityValidatorStrategy implements AsgValidatorStrategy {

    public static final String ERROR_1 = "Ontology Name doesn't match query Ontology reference";
    public static final String ERROR_2 = "No Elements After Start Node";
    public static final String ERROR_3 = "Start Node must be first element";

    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        Ontology.Accessor accessor = context.getOntologyAccessor();

        if (query.getStart().getNext().isEmpty())
            return new ValidationResult(false,this.getClass().getSimpleName(), ERROR_2);
        if (!query.getOnt().equals(accessor.name()))
            return new ValidationResult(false,this.getClass().getSimpleName(), ERROR_1);

        List<AsgEBase<EBase>> list = nextDescendants(query.getStart().getNext().get(0), Start.class);

        if (!list.isEmpty())
            return new ValidationResult(false,this.getClass().getSimpleName(), ERROR_3);


        return OK;
    }
    //endregion
}
