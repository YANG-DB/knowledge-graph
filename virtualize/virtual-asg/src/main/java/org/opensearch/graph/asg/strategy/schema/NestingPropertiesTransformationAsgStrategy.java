package org.opensearch.graph.asg.strategy.schema;

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


import javaslang.collection.Stream;
import org.opensearch.graph.asg.strategy.AsgStrategy;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.properties.*;
import org.opensearch.graph.unipop.schemaProviders.GraphElementPropertySchema;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class NestingPropertiesTransformationAsgStrategy implements AsgStrategy {

    public NestingPropertiesTransformationAsgStrategy( GraphElementSchemaProviderFactory schemaProviderFactory) {
        this.schemaProviderFactory = schemaProviderFactory;
    }

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
         Stream.ofAll(AsgQueryUtil.elements(query, EPropGroup.class))
                 .forEach(group -> group.geteBase().getProps().replaceAll(e->replace(context.getOntologyAccessor(),group,e)));
    }

    private EProp replace(Ontology.Accessor ontologyAccessor, AsgEBase<EPropGroup> groupAsgEBase,EProp eProp ) {
        //currently supporting only ETyped or EConcrete
        Optional<AsgEBase<ETyped>> eTypedAsgEBase = AsgQueryUtil.ancestor(groupAsgEBase, EEntityBase.class);
        if (!eTypedAsgEBase.isPresent()) {
            return eProp;
        }

        return replace(ontologyAccessor,eTypedAsgEBase.get().geteBase(),eProp);
    }

    private EProp replace(Ontology.Accessor ontologyAccessor, ETyped eType, EProp eProp) {
        // verify this eProp is representing a nested entity's property
        if(ontologyAccessor.cascadingElementFieldsPType(eType.geteType())
                .stream().noneMatch(p -> p.equals(eProp.getpType()))) return eProp;

        //if eProp is referring to a nested entity - wrap as a nested eProp with the appropriate 'path' field
        //check is this prop actually regarded as child mapping in the schema definition
        Optional<GraphElementPropertySchema> propertySchema = schemaProviderFactory.get(ontologyAccessor.get()).getPropertySchema(eProp.getpType());
        if(propertySchema.isPresent()) {
            if(propertySchema.get().getIndexingSchema(GraphElementPropertySchema.IndexingSchema.Type.nested).isPresent()) {
                String path = propertySchema.get().getIndexingSchema(GraphElementPropertySchema.IndexingSchema.Type.nested).get().getName();
                if (SchematicEProp.class.equals(eProp.getClass())) {
                    return new SchematicNestedEProp((SchematicEProp) eProp, path);
                }
                return new NestedEProp(eProp, path);
            }
        }
        return eProp;
    }


    //endregion

    private OntologyProvider ontologyProvider;
    private GraphElementSchemaProviderFactory schemaProviderFactory;

}
