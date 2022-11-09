package org.opensearch.graph.asg.strategy.schema;

/*-
 * #%L
 * virtual-asg
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





import org.opensearch.graph.asg.strategy.schema.utils.LikeUtil;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.Property;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.SchematicEProp;
import org.opensearch.graph.unipop.controller.utils.CollectionUtil;
import org.opensearch.graph.unipop.schema.providers.GraphElementPropertySchema;
import org.opensearch.graph.unipop.schema.providers.GraphElementSchemaProvider;
import org.opensearch.graph.unipop.schema.providers.GraphVertexSchema;
import javaslang.collection.Stream;
import org.opensearch.graph.asg.strategy.AsgElementStrategy;
import org.opensearch.graph.asg.strategy.AsgStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This strategy transforms 'like' predicates on fields which have a physical ngram mapping into an explicit schematic predicate
 *  - SchematicEProp
 */
public class LikeConstraintTransformationAsgStrategy implements AsgStrategy, AsgElementStrategy<EPropGroup> {
    //region Constructors
    public LikeConstraintTransformationAsgStrategy(OntologyProvider ontologyProvider, GraphElementSchemaProviderFactory schemaProviderFactory) {
        this.ontologyProvider = ontologyProvider;
        this.schemaProviderFactory = schemaProviderFactory;
    }
    //endregion

    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        AsgQueryUtil.elements(query, EPropGroup.class).forEach(ePropGroupAsgEBase -> {
            apply(query, ePropGroupAsgEBase, context);
        });
    }
    //endregion

    //region AsgElementStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgEBase<EPropGroup> ePropGroupAsgEBase, AsgStrategyContext context) {
        Optional<Ontology> ontology = this.ontologyProvider.get(query.getOnt());
        if (!ontology.isPresent()) {
            return;
        }

        Ontology.Accessor ont = new Ontology.Accessor(ontology.get());
        GraphElementSchemaProvider schemaProvider = this.schemaProviderFactory.get(ont.get());
        //currently supporting only ETyped or EConcrete
        Optional<AsgEBase<ETyped>> eTypedAsgEBase = AsgQueryUtil.ancestor(ePropGroupAsgEBase, EEntityBase.class);
        if (!eTypedAsgEBase.isPresent()) {
            return;
        }

        for (EProp eProp : new ArrayList<>(ePropGroupAsgEBase.geteBase().getProps())) {

            if (!eProp.isConstraint() || !eProp.getCon().getOp().equals(ConstraintOp.like)) {
                continue;
            }

            Iterable<GraphVertexSchema> vertexSchemas = schemaProvider.getVertexSchemas(eTypedAsgEBase.get().geteBase().geteType());
            if (Stream.ofAll(vertexSchemas).isEmpty()) {
                continue;
            }

            // currently supports a single vertex schema
            GraphVertexSchema vertexSchema = Stream.ofAll(vertexSchemas).get(0);

            Optional<Property> property = ont.$pType(eProp.getpType());
            if (!property.isPresent()) {
                continue;
            }

            Optional<GraphElementPropertySchema> propertySchema = vertexSchema.getProperty(property.get());
            if (!propertySchema.isPresent()) {
                continue;
            }

            Iterable<EProp> wildcardRuleEprops = LikeUtil.applyWildcardRules(eProp, propertySchema.get());
            List<EProp> ngramEprops = Stream.ofAll(wildcardRuleEprops)
                    .filter(wildcardEprop -> wildcardEprop.getCon().getOp().equals(ConstraintOp.like))
                    .map(wildcardEprop -> LikeUtil.getWildcardNgramsInsetProp(wildcardEprop, propertySchema.get()))
                    .filter(Optional::isPresent)
                    .map(ngramsEprop -> (SchematicEProp)ngramsEprop.get())
                    .flatMap(ngramsEprop -> Stream.ofAll(CollectionUtil.listFromObjectValue(ngramsEprop.getCon().getExpr()))
                            .map(word -> (EProp)new SchematicEProp(
                                    0,
                                    ngramsEprop.getpType(),
                                    ngramsEprop.getSchematicName(),
                                    Constraint.of(ConstraintOp.eq, word)
                                    )))
                    .appendAll(Stream.ofAll(wildcardRuleEprops)
                        .filter(eprop -> eprop.getCon().getOp().equals(ConstraintOp.eq)))
                    .toJavaList();

            ePropGroupAsgEBase.geteBase().getProps().remove(eProp);
            ePropGroupAsgEBase.geteBase().getProps().addAll(ngramEprops);
            ePropGroupAsgEBase.geteBase().getProps().addAll(
                    Stream.ofAll(wildcardRuleEprops)
                            .filter(eprop -> eprop.getCon().getOp().equals(ConstraintOp.like))
                            .toJavaList());
        }
    }
    //endregion

    //region Fields
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    private OntologyProvider ontologyProvider;
    //endregion
}
