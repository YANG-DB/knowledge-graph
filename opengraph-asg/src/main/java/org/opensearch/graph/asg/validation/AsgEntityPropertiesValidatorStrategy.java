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







import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.EntityType;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.entity.EUntyped;
import org.opensearch.graph.model.query.entity.Typed;
import org.opensearch.graph.model.query.properties.CalculatedEProp;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.opensearch.graph.model.query.quant.Quant1;
import org.opensearch.graph.model.validation.ValidationResult;
import javaslang.collection.Stream;

import java.util.*;
import java.util.stream.Collectors;

import static org.opensearch.graph.model.asgQuery.AsgQueryUtil.calculateNextAncestor;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.ignorableConstraints;
import static org.opensearch.graph.model.validation.ValidationResult.OK;
import static org.opensearch.graph.model.validation.ValidationResult.print;

public class AsgEntityPropertiesValidatorStrategy implements AsgValidatorStrategy {

    public static final String ERROR_1 = "No Parent Element found  ";
    public static final String ERROR_2 = "Property type mismatch parent entity";
    public static final String ERROR_3 = "No %s type found for constraint %s";

    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();

        Ontology.Accessor accessor = context.getOntologyAccessor();
        Stream.ofAll(AsgQueryUtil.elements(query, EProp.class))
                .filter(property -> !(property.geteBase() instanceof CalculatedEProp))
                .forEach(property -> {
                    Optional<AsgEBase<EEntityBase>> parent = calculateNextAncestor(property,EEntityBase.class,Collections.singletonList(Quant1.class));
                    if (!parent.isPresent()) {
                        errors.add(ERROR_1 + ":" + property);
                    } else {
                        errors.addAll(check(accessor, parent.get(), property.geteBase()));
                    }
                });

        Stream.ofAll(AsgQueryUtil.elements(query, EPropGroup.class))
                .forEach(group -> {
                    Optional<AsgEBase<EEntityBase>> parent = calculateNextAncestor(group,EEntityBase.class,Collections.singletonList(Quant1.class));
                    if (!parent.isPresent()) {
                        errors.add(ERROR_1 + group);
                    } else {
                        errors.addAll(check(accessor, parent.get(), group.geteBase()));
                    }
                });
        if (errors.isEmpty())
            return OK;

        return new ValidationResult(false, this.getClass().getSimpleName(), errors.toArray(new String[errors.size()]));
    }
    //endregion

    protected List<String> check(Ontology.Accessor accessor, AsgEBase<EEntityBase> base, EPropGroup property) {
        return property.getProps().stream()
                .filter(prop->!CalculatedEProp.class.isAssignableFrom(prop.getClass()))
                .map(prop->check(accessor,base,prop))
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    protected List<String> check(Ontology.Accessor accessor, AsgEBase<EEntityBase> base, EProp property) {
        List<String> errors = new ArrayList<>();
        if (base.geteBase() instanceof Typed.eTyped) {
            EntityType entityType = accessor.$entity$(((Typed.eTyped) base.geteBase()).geteType());
            String pType = property.getpType();

            if (accessor.cascadingElementFieldsPName(entityType.geteType()).stream().noneMatch(p -> p.equals(pType))) {
                errors.add(ERROR_2 + ":" + print(base, property));
            }

        } else if (base.geteBase() instanceof EUntyped) {
            Stream<String> types = Stream.ofAll(((EUntyped) base.geteBase()).getvTypes()).map(accessor::$entity$)
                    .flatMap(e->accessor.cascadingElementFieldsPName(e.geteType()));
            String pType = property.getpType();

            //skip projection fields validation
            if(property.getProj()==null) {
                if (types.toJavaStream().noneMatch(p -> p.equals(pType))) {
                    errors.add(ERROR_2 + ":" + print(base, property));
                }
            }
        }

        // if projection type prop -> dont check constraints
        if(property.getProj()!=null) {
            return errors;
        }

        if(ignorableConstraints.contains(property.getCon().getClass()))
            return errors;

        //interval type
        if(property.getCon().getiType()==null) {
            errors.add(String.format(ERROR_3 ," interval type ",property));
        }

        //expresion
        if (!Arrays.asList(ConstraintOp.empty, ConstraintOp.notEmpty).contains(property.getCon().getOp())) {
            if (property.getCon().getExpr() == null) {
                errors.add(String.format(ERROR_3, " expression ", property));
            }
        }

        //operation
        if(property.getCon().getOp()==null) {
            errors.add(String.format(ERROR_3 ," operation ",property));
        }
        return errors;
    }

}
