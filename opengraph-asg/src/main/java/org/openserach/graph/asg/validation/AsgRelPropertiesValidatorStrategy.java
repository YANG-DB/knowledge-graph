package org.openserach.graph.asg.validation;



import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.RelationshipType;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.properties.RelProp;
import org.opensearch.graph.model.query.properties.RelPropGroup;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.opensearch.graph.model.validation.ValidationResult;

import java.util.*;
import java.util.stream.Collectors;

import static org.opensearch.graph.model.asgQuery.AsgQueryUtil.elements;
import static org.opensearch.graph.model.asgQuery.AsgQueryUtil.nextDescendants;
import static org.opensearch.graph.model.validation.ValidationResult.OK;
import static org.opensearch.graph.model.validation.ValidationResult.print;

public class AsgRelPropertiesValidatorStrategy implements AsgValidatorStrategy {

    public static final String ERROR_2 = "Property type mismatch parent Relation ";
    public static final String ERROR_3 = "No %s type found for constraint %s";

    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();
        Ontology.Accessor accessor = context.getOntologyAccessor();
        List<AsgEBase<Rel>> list = nextDescendants(query.getStart(), Rel.class);

        list.forEach(rel -> {
            if (!rel.getB().isEmpty()) {
                AsgEBase<? extends EBase> asgEBase = rel.getB().get(0);
                if (asgEBase.geteBase() instanceof RelProp) {
                    errors.addAll(check(accessor, rel, ((RelProp) asgEBase.geteBase())));
                } else if (asgEBase.geteBase() instanceof RelPropGroup) {
                    errors.addAll(check(accessor, rel, ((RelPropGroup) asgEBase.geteBase())));
                }
            }
        });

        if (errors.isEmpty())
            return OK;

        return new ValidationResult(false, this.getClass().getSimpleName(), errors.toArray(new String[errors.size()]));
    }
    //endregion

    private List<String> check(Ontology.Accessor accessor, AsgEBase<Rel> base, RelPropGroup property) {
        return property.getProps().stream().map(prop -> check(accessor, base, prop))
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    private List<String> check(Ontology.Accessor accessor, AsgEBase<Rel> base, RelProp property) {
        List<String> errors = new ArrayList<>();
        RelationshipType relationshipType = accessor.$relation$(base.geteBase().getrType());
        String pType = property.getpType();

        if (!property.isAggregation() && relationshipType.fields().stream().noneMatch(p -> p.equals(pType))) {
            errors.add(ERROR_2 + ":" + print(base, property));
        }

        // if projection type prop -> dont check constraints
        if (property.getProj() != null) {
            return errors;
        }

        //interval type
        if (property.getCon().getiType() == null) {
            errors.add(String.format(ERROR_3, " interval type ", property));
        }

        //expresion
        if (!Arrays.asList(ConstraintOp.empty, ConstraintOp.notEmpty).contains(property.getCon().getOp())) {
            if (property.getCon().getExpr() == null) {
                errors.add(String.format(ERROR_3, " expression ", property));
            }
        }

        //operation
        if (property.getCon().getCountOp() == null && property.getCon().getOp() == null) {
            errors.add(String.format(ERROR_3, " operation ", property));
        }

        return errors;
    }
}
