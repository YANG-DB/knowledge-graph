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





import org.opensearch.graph.asg.strategy.AsgStrategy;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.Property;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.properties.*;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.opensearch.graph.model.query.properties.projection.IdentityProjection;
import org.opensearch.graph.unipop.schemaProviders.GraphElementPropertySchema;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;
import org.opensearch.graph.unipop.schemaProviders.GraphVertexSchema;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

import static org.opensearch.graph.unipop.schemaProviders.GraphElementPropertySchema.IndexingSchema.Type.exact;

public class ExactConstraintTransformationAsgStrategy implements AsgStrategy {
    //region Constructors
    public ExactConstraintTransformationAsgStrategy(
            OntologyProvider ontologyProvider,
            GraphElementSchemaProviderFactory schemaProviderFactory) {
        this.ontologyProvider = ontologyProvider;
        this.schemaProviderFactory = schemaProviderFactory;

        this.includedOps = Stream.of(ConstraintOp.eq, ConstraintOp.ne, ConstraintOp.gt,
                ConstraintOp.ge, ConstraintOp.lt, ConstraintOp.le, ConstraintOp.empty, ConstraintOp.notEmpty,
                ConstraintOp.inSet, ConstraintOp.notInSet, ConstraintOp.inRange, ConstraintOp.notInRange, ConstraintOp.likeAny)
                .toJavaSet();
    }
    //endregion

    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Optional<Ontology> ontology = this.ontologyProvider.get(query.getOnt());
        if (!ontology.isPresent()) {
            return;
        }

        Ontology.Accessor ont = new Ontology.Accessor(ontology.get());
        GraphElementSchemaProvider schemaProvider = this.schemaProviderFactory.get(ont.get());

        AsgQueryUtil.elements(query, EPropGroup.class).forEach(ePropGroupAsgEBase -> {
            //currently supporting only ETyped or EConcrete
            Optional<AsgEBase<ETyped>> eTypedAsgEBase = AsgQueryUtil.ancestor(ePropGroupAsgEBase, EEntityBase.class);
            if (!eTypedAsgEBase.isPresent()) {
                return;
            }

            transformEPropGroup(ont, schemaProvider, ePropGroupAsgEBase.geteBase(), eTypedAsgEBase.get());
        });
    }

    private void transformEPropGroup(Ontology.Accessor ont, GraphElementSchemaProvider schemaProvider, EPropGroup ePropGroup, AsgEBase<ETyped> eTypedAsgEBase) {
        for (EProp eProp : new ArrayList<>(ePropGroup.getProps())) {
            if (eProp.getCon()!=null && !this.includedOps.contains(eProp.getCon().getOp())) {
                continue;
            }

            Iterable<GraphVertexSchema> vertexSchemas = schemaProvider.getVertexSchemas(eTypedAsgEBase.geteBase().geteType());
            if (Stream.ofAll(vertexSchemas).isEmpty()) {
                continue;
            }

            // currently supporting a single vertex schema
            GraphVertexSchema vertexSchema = Stream.ofAll(vertexSchemas).get(0);

            Optional<Property> property = ont.$pType(eTypedAsgEBase.geteBase().geteType(),eProp.getpType());
            if (!property.isPresent()) {
                continue;
            }

            Optional<GraphElementPropertySchema> propertySchema = vertexSchema.getProperty(property.get());
            if (!propertySchema.isPresent()) {
                continue;
            }

            Optional<GraphElementPropertySchema.ExactIndexingSchema> exactIndexingSchema = propertySchema.get().getIndexingSchema(exact);
            if (!exactIndexingSchema.isPresent()) {
                // should throw an error?
                throw new IllegalStateException("should have exact schema index");
            }

            ePropGroup.getProps().remove(eProp);
            if(eProp instanceof RankingProp){
                ePropGroup.getProps().add(new SchematicRankedEProp(
                        0,
                        eProp.getpType(),
                        exactIndexingSchema.get().getName(),
                        eProp.getCon(),
                        ((RankingProp) eProp).getBoost()));
            }else if(eProp.isConstraint()){
                ePropGroup.getProps().add(new SchematicEProp(
                        0,
                        eProp.getpType(),
                        exactIndexingSchema.get().getName(),
                        eProp.getCon()));
            }else if(eProp.isProjection()) {
                ePropGroup.getProps().add(new EProp(
                        0,
                        eProp.getpType(),
                        new IdentityProjection()));
            }
        }

        ePropGroup.getGroups().forEach(group -> transformEPropGroup(ont, schemaProvider, group, eTypedAsgEBase));
    }

    //endregion

    //region Fields
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    private OntologyProvider ontologyProvider;

    private Set<ConstraintOp> includedOps;
    //endregion
}
