package org.opensearch.graph.unipop.schema.providers;

/*-
 * #%L
 * virtual-unipop
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

import org.opensearch.graph.model.ontology.EntityType;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.Property;
import org.opensearch.graph.model.resourceInfo.GraphError;
import org.opensearch.graph.model.schema.Entity;
import org.opensearch.graph.model.schema.IndexProvider;
import org.opensearch.graph.model.schema.MappingIndexType;
import org.opensearch.graph.model.schema.PartitionType;
import org.opensearch.graph.unipop.schema.providers.indexPartitions.NestedIndexPartitions;
import org.opensearch.graph.unipop.schema.providers.indexPartitions.StaticIndexPartitions;
import org.opensearch.graph.unipop.schema.providers.indexPartitions.TimeBasedIndexPartitions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.opensearch.graph.model.GlobalConstants.Mapping.KEYWORD;
import static org.opensearch.graph.model.GlobalConstants.Scalars.TEXT;
import static org.opensearch.graph.model.schema.MappingIndexType.*;
import static org.opensearch.graph.model.schema.PartitionType.EMBEDDED;
import static org.opensearch.graph.unipop.schema.providers.GraphElementPropertySchema.IndexingSchema.Type.nested;

public interface GraphVertexSchemaUtils {

    static List<GraphVertexSchema> generateGraphVertexSchema(Ontology.Accessor accessor, IndexProvider provider, Entity e) {
        List<GraphVertexSchema> elements = _generateGraphVertexSchema(accessor, provider, e);

        //first do the nesting vertex schema creation
        List<GraphVertexSchema> collect = e.getNested().stream()
                .flatMap(ne -> generateGraphVertexSchema(accessor, provider, ne).stream())
                .collect(Collectors.toList());

        // finally do the parent vertex schema creation
        elements.forEach(schema -> schema.setNestedSchemas(collect));
        // finally fix nested properties index schema according to real nesting
        elements.forEach(schema -> fixNestedPropertiesSchema(accessor,schema));
        return elements;
    }

    static private List<GraphVertexSchema> _generateGraphVertexSchema(Ontology.Accessor accessor, IndexProvider provider, Entity e) {
        MappingIndexType type = valueOf(e.getPartition().toUpperCase());
        switch (type) {
            case UNIFIED:
            case STATIC:
                //todo verify correctness
                return e.getProps().getValues().stream()
                        .map(v -> new GraphVertexSchema.Impl(
                                e.getType(),
                                new StaticIndexPartitions(v),
                                generateGraphElementPropertySchemas(accessor, provider, STATIC, v, e.getType().getName())))
                        .collect(Collectors.toList());
            case NESTED:
                return e.getProps().getValues().stream()
                        .map(v -> new GraphVertexSchema.Impl(
                                e.getType(),
                                new NestedIndexPartitions(v),
                                generateGraphElementPropertySchemas(accessor, provider, NESTED, v, e.getType().getName())))
                        .collect(Collectors.toList());
            case TIME:
                return e.getProps().getValues().stream()
                        .map(v -> new GraphVertexSchema.Impl(
                                e.getType(),
                                new TimeBasedIndexPartitions(e.getProps()),
                                generateGraphElementPropertySchemas(accessor, provider, TIME, v, e.getType().getName())))
                        .collect(Collectors.toList());
        }
        //default - when other partition type is declared
        String v = e.getProps().getValues().isEmpty() ? e.getType().getName() : e.getProps().getValues().get(0);
        return Collections.singletonList(
                new GraphVertexSchema.Impl(
                        e.getType(),
                        new StaticIndexPartitions(v),
                        generateGraphElementPropertySchemas(accessor, provider, STATIC, v, e.getType().getName())));
    }

    static private List<GraphElementPropertySchema> generateGraphElementPropertySchemas(Ontology.Accessor accessor, IndexProvider provider, MappingIndexType mappingIndexType, String mappingName, String type) {
        List<GraphError> schemaValidationErrors = new ArrayList<>();
        Optional<EntityType> entity = accessor.entity(type);
        if (!entity.isPresent())
            throw new GraphError.GraphErrorException(new GraphError(String.format("No Schema element found for %s", type),
                    "Error Creating Index Schema"));

        EntityType entityType = entity.get();
        List<GraphElementPropertySchema> elementPropertySchemas = new ArrayList<>();
        for (String property : accessor.cascadingElementFieldsPType(entityType.geteType())) {
            Optional<GraphElementPropertySchema> propertySchema = generatePropertySchema(accessor, provider, schemaValidationErrors, mappingIndexType, mappingName, entityType, property);
            propertySchema.ifPresent(elementPropertySchemas::add);
        }

        if (schemaValidationErrors.isEmpty())
            return elementPropertySchemas;

        throw new GraphError.GraphErrorException(schemaValidationErrors);
    }

    static private Optional<GraphElementPropertySchema> generatePropertySchema(Ontology.Accessor accessor, IndexProvider provider,
                                                                               List<GraphError> schemaValidationErrors,
                                                                               MappingIndexType mappingIndexType,
                                                                               String mappingName,
                                                                               EntityType entityType,
                                                                               String propertyKey) {
        Optional<Property> prop = accessor.$pType(entityType.geteType(), propertyKey);
        if (!prop.isPresent()) {
            schemaValidationErrors.add(new GraphError(String.format("No Schema element found for %s", propertyKey), "Error Creating Index Schema"));
            return Optional.empty();
        } else {
            Property property = prop.get();

            GraphElementPropertySchema.Impl propertySchema = new GraphElementPropertySchema.Impl(propertyKey,
                    property.getpType(),
                    property.getType(),
                    new ArrayList<>());

            if (TEXT.equals(property.getType())) {
                propertySchema.addIndexSchema(new GraphElementPropertySchema.ExactIndexingSchema.Impl(property.getpType() + "." + KEYWORD));
            }
            if (mappingIndexType == NESTED) {
                propertySchema.addIndexSchema(new GraphElementPropertySchema.NestedIndexingSchema.Impl(mappingName));
            } else if (property instanceof Property.NestedProperty) {
                Optional<Entity> providerEntity;
                //cascading nested fields - for derivative entities
                if (provider.getTopLevelEntities(property.getType()).isPresent()) {
                    providerEntity = provider.getTopLevelEntities(property.getType());
                } else if (provider.getEntity(property.getType()).isPresent()) {
                    providerEntity = provider.getEntity(property.getType());
                } else {
                    providerEntity = provider.getTopLevelEntities(((Property.NestedProperty) property).getContainerType()).isPresent() ?
                            provider.getTopLevelEntities(((Property.NestedProperty) property).getContainerType()) :
                            provider.getEntity(((Property.NestedProperty) property).getContainerType());
                }
                //if provider entity is nested - add an appropriate index schema
                providerEntity.ifPresent(entity ->
                        checkIfNestedAndAddIndexSchema(accessor, provider, entity, property, mappingName)
                                .forEach(propertySchema::addIndexSchema));
            }

            return Optional.of(propertySchema);
        }
    }

    static private List<GraphElementPropertySchema.IndexingSchema> checkIfNestedAndAddIndexSchema(Ontology.Accessor accessor, IndexProvider provider, Entity providerEntity, Property property, String mappingName) {
        List<GraphElementPropertySchema.IndexingSchema> schemas = new ArrayList<>();
        if (MappingIndexType.valueOf(providerEntity.getPartition().toUpperCase()).equals(NESTED)) {
            if (providerEntity.hasProperties()) {
                schemas.add(new GraphElementPropertySchema.NestedIndexingSchema.Impl(providerEntity.getProps().getValues().get(0)));
            } else {
                schemas.add(new GraphElementPropertySchema.NestedIndexingSchema.Impl(mappingName));
            }
            //if provider entity is an embedded one - seek its container to verify it is not nested itself
        } else if (PartitionType.valueOf(providerEntity.getMapping().toUpperCase()).equals(EMBEDDED)) {
            Optional<EntityType> parent = accessor.nestedParent(providerEntity.getType().getName(), property.getpType());

            if (!parent.isPresent()) return schemas;
            if (!provider.getEntity(property.getType()).isPresent()) return schemas;

            // check is the entity nested
            checkIfNestedAndAddIndexSchema(accessor, provider, provider.getEntity(property.getType()).get(), property, mappingName);
        }
        return schemas;
    }


    static GraphVertexSchema fixNestedPropertiesSchema(Ontology.Accessor accessor,GraphVertexSchema vertexSchema ) {
        //fix nesting schema properties
        vertexSchema.getNestedSchemas().forEach(nestedSchema -> {
            //assuming nestedSchema was validated and all entity name are reported
            List<Property> properties = accessor.nestedEntityFieldName(accessor.$entity$(vertexSchema.getLabel().getName()),
                    accessor.$entity$(nestedSchema.getLabel().getName()));

            //only replace nested index-schema for fields that are cascading from the nesting entities for this parent entity
            if(!properties.isEmpty()) {
                nestedSchema.getProperties().forEach(nestedProp ->
                        vertexSchema.getProperty(properties.get(0).getpType() + "." + nestedProp.getName())
                                .map(p -> p.addIndexSchemas(nestedProp.getIndexingSchemes(),i->i.getType().equals(nested))));
            }
        });

        return vertexSchema;
    }
}
