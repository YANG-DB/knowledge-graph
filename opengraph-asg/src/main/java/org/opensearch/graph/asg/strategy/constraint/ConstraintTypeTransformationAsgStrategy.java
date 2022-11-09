package org.opensearch.graph.asg.strategy.constraint;

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






import org.opensearch.graph.asg.strategy.AsgStrategy;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.asg.util.OntologyPropertyTypeFactory;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.ontology.Property;
import org.opensearch.graph.model.ontology.Value;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.RelProp;

import java.util.*;

import static org.opensearch.graph.model.asgQuery.AsgQueryUtil.getEprops;
import static org.opensearch.graph.model.asgQuery.AsgQueryUtil.getRelProps;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.*;

/**
 * This strategy replaces string literals with enumeration ordinal values in case the string literal belongs to an enumeration field.
 */
public class ConstraintTypeTransformationAsgStrategy implements AsgStrategy {
    //region Constructors
    public ConstraintTypeTransformationAsgStrategy() {
        this.singleValueOps = ConstraintOp.singleValueOps;
    }
    //endregion

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        getEprops(query).stream()
                .filter(prop -> prop.getCon() != null)
                .forEach(eProp -> applyExpressionTransformation(context, eProp, EProp.class));

        getRelProps(query).stream()
                .filter(prop -> prop.getCon() != null)
                .forEach(relProp -> applyExpressionTransformation(context, relProp, RelProp.class));
    }

    //region Private Methods

    private void applyExpressionTransformation(AsgStrategyContext context, EBase eBase, Class klass) {
        if (klass == EProp.class) {
            EProp eProp = (EProp) eBase;
            Optional<Property> property = context.getOntologyAccessor().$pType(eProp.getpType());

            ConstraintOp op = eProp.getCon().getOp();
            if (property.isPresent() && context.getOntologyAccessor().enumeratedType(property.get().getType()).isPresent()) {
                //replace string content with enum value
                Constraint newCon = eProp.getCon().clone();
                newCon.setExpr(context.getOntologyAccessor().enumeratedType$(property.get().getType()).valueOf(eProp.getCon().getExpr().toString())
                        .orElse(new Value(-1, eProp.getCon().getExpr().toString()))
                        .getVal());
                eProp.setCon(newCon);
            } else if (property.isPresent() && isSingleElementOp(op) && !ignorableConstraints.contains(eProp.getCon().getClass())) {
                Constraint newCon = eProp.getCon().clone();
                newCon.setExpr(new OntologyPropertyTypeFactory().supply(property.get(), eProp.getCon().getExpr()));
                eProp.setCon(newCon);
            }
        }
        if (klass == RelProp.class) {
            RelProp relProp = (RelProp) eBase;
            Optional<Property> property = context.getOntologyAccessor().$pType(relProp.getpType());
            if (property.isPresent() && context.getOntologyAccessor().enumeratedType(property.get().getType()).isPresent()) {
                //replace string content with enum value
                Constraint newCon = relProp.getCon().clone();
                newCon.setExpr(context.getOntologyAccessor().enumeratedType$(property.get().getType()).valueOf(relProp.getCon().getExpr().toString())
                        .orElse(new Value(-1, relProp.getCon().getExpr().toString()))
                        .getVal());
                relProp.setCon(newCon);
            } else if (relProp.getCon() != null) {
                ConstraintOp op = relProp.getCon().getOp();
                if (property.isPresent() && isSingleElementOp(op) && !ignorableConstraints.contains(relProp.getCon().getClass())) {
                    Constraint newCon = new Constraint(op, new OntologyPropertyTypeFactory().supply(property.get(), relProp.getCon().getExpr()));
                    relProp.setCon(newCon);
                }
            }
        }
    }

    private boolean isSingleElementOp(ConstraintOp op) {
        return singleValueOps.contains(op);
    }
    //endregion

    //region Fields
    private Set<ConstraintOp> singleValueOps;
    //endregion

}




