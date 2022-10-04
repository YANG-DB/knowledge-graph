package org.opensearch.graph.model.ontology;

/*-
 * #%L
 * opengraph-model
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


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.opensearch.graph.model.ontology.Property.NestedProperty;
import org.opensearch.graph.model.resourceInfo.GraphError;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.opensearch.graph.model.schema.Entity;

import java.awt.geom.Point2D;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.stream.Stream.*;

/**
 * Created by benishue on 22-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties({"primitiveTypes"})
public class Ontology {
    public Ontology(Ontology source) {
        this();
        //copy
        entityTypes.addAll(source.getEntityTypes().stream().map(EntityType::clone).collect(Collectors.toList()));
        relationshipTypes.addAll(source.getRelationshipTypes().stream().map(RelationshipType::clone).collect(Collectors.toList()));
        enumeratedTypes.addAll(source.getEnumeratedTypes().stream().map(EnumeratedType::clone).collect(Collectors.toList()));
        metadata.addAll(source.metadata.stream().map(Property::clone).collect(Collectors.toList()));
        properties.addAll(source.getProperties().stream().map(Property::clone).collect(Collectors.toSet()));
        directives.addAll(source.getDirectives());
        compositeTypes.addAll(source.getCompositeTypes());
    }

    public Ontology() {
        initCollections();
        initPrimitives();
    }

    private void initCollections() {
        directives = new ArrayList<>();
        entityTypes = new ArrayList<>();
        relationshipTypes = new ArrayList<>();
        enumeratedTypes = new ArrayList<>();
        properties = new HashSet<>();
        metadata = new ArrayList<>();
        compositeTypes = new ArrayList<>();
    }

    private void initPrimitives() {
        primitiveTypes = new ArrayList<>();
        primitiveTypes.add(new PrimitiveType("int", Long.class));
        primitiveTypes.add(new PrimitiveType("string", String.class));
        primitiveTypes.add(new PrimitiveType("text", String.class));
        primitiveTypes.add(new PrimitiveType("float", Double.class));
        primitiveTypes.add(new PrimitiveType("date", Date.class));
        primitiveTypes.add(new PrimitiveType("datetime", Date.class));
        primitiveTypes.add(new PrimitiveType("geo_point", Point2D.class));
        primitiveTypes.add(new PrimitiveType("array", Array.class));
    }

    //region Getters & Setters

    public String getOnt() {
        return ont;
    }

    public void setOnt(String ont) {
        this.ont = ont;
    }

    public List<EntityType> getEntityTypes() {
        return entityTypes;
    }

    public Set<Property> getProperties() {
        return this.properties;
    }

    public void setProperties(Set<Property> properties) {
        this.properties = properties;
    }

    public void setEntityTypes(List<EntityType> entityTypes) {
        this.entityTypes = entityTypes;
    }

    public List<RelationshipType> getRelationshipTypes() {
        return relationshipTypes;
    }

    public List<DirectiveType> getDirectives() {
        return directives;
    }

    public void setDirectives(List<DirectiveType> directives) {
        this.directives = directives;
    }

    public void setRelationshipTypes(List<RelationshipType> relationshipTypes) {
        this.relationshipTypes = relationshipTypes;
    }

    public List<EnumeratedType> getEnumeratedTypes() {
        return enumeratedTypes;
    }

    public void setEnumeratedTypes(List<EnumeratedType> enumeratedTypes) {
        this.enumeratedTypes = enumeratedTypes;
    }

    public List<CompositeType> getCompositeTypes() {
        return compositeTypes;
    }

    public void setCompositeTypes(List<CompositeType> compositeTypes) {
        this.compositeTypes = compositeTypes;
    }

    public List<PrimitiveType> getPrimitiveTypes() {
        return primitiveTypes;
    }

//endregion

    //region Public Methods

    @Override
    public String toString() {
        return "Ontology [enumeratedTypes = " + enumeratedTypes + ", ont = " + ont + ", relationshipTypes = " + relationshipTypes + ", entityTypes = " + entityTypes + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ontology ontology = (Ontology) o;
        return ont.equals(ontology.ont) &&
                directives.equals(ontology.directives) &&
                entityTypes.equals(ontology.entityTypes) &&
                relationshipTypes.equals(ontology.relationshipTypes) &&
                properties.equals(ontology.properties) &&
                metadata.equals(ontology.metadata) &&
                enumeratedTypes.equals(ontology.enumeratedTypes) &&
                compositeTypes.equals(ontology.compositeTypes) &&
                primitiveTypes.equals(ontology.primitiveTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ont, directives, entityTypes, relationshipTypes, properties, metadata, enumeratedTypes, compositeTypes, primitiveTypes);
    }

    //endregion

    //region Fields
    private String ont;
    private List<DirectiveType> directives;
    private List<EntityType> entityTypes;
    private List<RelationshipType> relationshipTypes;
    private Set<Property> properties;
    private List<Property> metadata;
    private List<EnumeratedType> enumeratedTypes;
    private List<CompositeType> compositeTypes;
    private List<PrimitiveType> primitiveTypes;
    //endregion

    //region Builder

    public static final class OntologyBuilder {
        private String ont = "Generic";
        private List<DirectiveType> directives;
        private List<EntityType> entityTypes;
        private List<RelationshipType> relationshipTypes;
        private LinkedHashSet<Property> properties;
        private List<EnumeratedType> enumeratedTypes;
        private List<CompositeType> compositeTypes;

        private OntologyBuilder() {
            this.directives = new ArrayList<>();
            this.entityTypes = new ArrayList<>();
            this.relationshipTypes = new ArrayList<>();
            this.properties = new LinkedHashSet<>();
            this.enumeratedTypes = new ArrayList<>();
            this.compositeTypes = new ArrayList<>();
        }

        public static OntologyBuilder anOntology(String ont) {
            return new OntologyBuilder().withOnt(ont);
        }

        public OntologyBuilder withOnt(String ont) {
            this.ont = ont;
            return this;
        }

        public OntologyBuilder withEntityTypes(List<EntityType> entityTypes) {
            this.entityTypes = entityTypes;
            return this;
        }

        public OntologyBuilder addEntityTypes(List<EntityType> entityTypes) {
            this.entityTypes.addAll(entityTypes);
            return this;
        }

        public OntologyBuilder addEntityType(EntityType entityType) {
            this.entityTypes.add(entityType);
            return this;
        }

        public Optional<EntityType> getEntityType(String entityType) {
            return this.entityTypes.stream().filter(et -> et.geteType().equals(entityType)).findAny();
        }

        public OntologyBuilder withRelationshipTypes(List<RelationshipType> relationshipTypes) {
            this.relationshipTypes = relationshipTypes;
            return this;
        }

        public OntologyBuilder addRelationshipTypes(List<RelationshipType> relationshipTypes) {
            this.relationshipTypes.addAll(relationshipTypes);
            return this;
        }

        public Optional<RelationshipType> getRelationshipType(String relationshipType) {
            return this.relationshipTypes.stream().filter(et -> et.getrType().equals(relationshipType)).findAny();
        }

        public OntologyBuilder addRelationshipType(RelationshipType relationshipType) {
            this.relationshipTypes.add(relationshipType);
            return this;
        }

        public Optional<Property> getProperty(String property) {
            return this.properties.stream().filter(et -> et.getName().equals(property)).findAny();
        }

        public OntologyBuilder withDirective(DirectiveType directive) {
            this.directives.add(directive);
            return this;

        }

        public OntologyBuilder withDirectives(List<DirectiveType> directives) {
            this.directives.addAll(directives);
            return this;

        }

        public OntologyBuilder withEnumeratedTypes(List<EnumeratedType> enumeratedTypes) {
            this.enumeratedTypes = enumeratedTypes;
            return this;
        }

        public OntologyBuilder addEnumeratedTypes(EnumeratedType enumeratedType) {
            this.enumeratedTypes.add(enumeratedType);
            return this;
        }

        public OntologyBuilder withCompositeTypes(List<CompositeType> compositeTypes) {
            this.compositeTypes = compositeTypes;
            return this;
        }

        public OntologyBuilder withProperties(Set<Property> properties) {
            this.properties = new LinkedHashSet<>(properties);
            return this;
        }

        public OntologyBuilder addProperty(Property property) {
            this.properties.add(property);
            return this;
        }

        public OntologyBuilder addProperties(List<Property> properties) {
            this.properties.addAll(properties);
            return this;
        }

        public List<RelationshipType> getRelationships() {
            return relationshipTypes;
        }

        public List<EntityType> getEntityTypes() {
            return entityTypes;
        }

        public Ontology build() {
            Ontology ontology = new Ontology();
            ontology.setOnt(ont);
            ontology.setDirectives(directives);
            ontology.setEntityTypes(entityTypes);
            ontology.setRelationshipTypes(relationshipTypes);
            ontology.setEnumeratedTypes(enumeratedTypes);
            ontology.setCompositeTypes(compositeTypes);
            ontology.setProperties(properties);
            return ontology;
        }

    }

    //endregion

    //region Accessor
    public static class Accessor implements Supplier<Ontology> {
        //region Constructors
        public Accessor(Ontology ontology) {
            this.ontology = ontology;

            this.entitiesByEtype = Stream.ofAll(ontology.getEntityTypes())
                    .toJavaMap(entityType -> new Tuple2<>(entityType.geteType(), entityType));
            this.entitiesByName = Stream.ofAll(ontology.getEntityTypes())
                    .toJavaMap(entityType -> new Tuple2<>(entityType.getName(), entityType));

            this.relationsByRtype = Stream.ofAll(ontology.getRelationshipTypes())
                    .toJavaMap(relationshipType -> new Tuple2<>(relationshipType.getrType(), relationshipType));
            this.relationsByName = Stream.ofAll(ontology.getRelationshipTypes())
                    .toJavaMap(relationshipType -> new Tuple2<>(relationshipType.getName(), relationshipType));

            this.propertiesByPtype = Stream.ofAll(ontology.getProperties())
                    .toJavaMap(property -> new Tuple2<>(property.getpType(), property));
            this.propertiesByName = Stream.ofAll(ontology.getProperties())
                    .toJavaMap(property -> new Tuple2<>(property.getName(), property));
        }
        //endregion

        //region Public Methods
        @Override
        public Ontology get() {
            return this.ontology;
        }

        public String name() {
            return this.ontology.getOnt();
        }

        public Optional<? extends BaseElement> $element(String type) {
            if (this.entitiesByEtype.get(type) != null)
                return Optional.of(this.entitiesByEtype.get(type));
            if (this.relationsByName.get(type) != null)
                return Optional.of(this.relationsByName.get(type));
            return Optional.empty();
        }

        /**
         * get entity by its type - return optional with empty if not found
         *
         * @param eType
         * @return
         */
        public Optional<EntityType> $entity(String eType) {
            return Optional.ofNullable(this.entitiesByEtype.get(eType));
        }

        /**
         * get directive by its name
         *
         * @param name
         * @return
         */
        public Optional<DirectiveType> $directive(String name) {
            return this.ontology.directives.stream().filter(d -> d.getName().equals(name)).findFirst();
        }

        /**
         * get directive by its name - if not found throws error
         *
         * @param name
         * @return
         */
        public DirectiveType $directive$(String name) {
            return this.ontology.directives.stream().filter(d -> d.getName().equals(name)).findFirst()
                    .orElseThrow(() -> new GraphError.GraphErrorException(new GraphError("No Ontology $directive$ for value ", "No Ontology $directive$ for value[" + name + "]")));
        }

        /**
         * get entity by its type - if not found throws error
         *
         * @param eType
         * @return
         */
        public EntityType $entity$(String eType) {
            return $entity(eType)
                    .orElseThrow(() -> new GraphError.GraphErrorException(new GraphError("No Ontology entity for value ", "No Ontology entity for value[" + eType + "]")));
        }

        /**
         * get entity by its name- returns optional with empty if not found
         *
         * @param entityName
         * @return
         */
        public Optional<EntityType> entity(String entityName) {
            return Optional.ofNullable(this.entitiesByName.get(entityName));
        }

        /**
         * get entity by its type - throws error if not found
         *
         * @param entityName
         * @return
         */
        public EntityType entity$(String entityName) {
            return entity(entityName)
                    .orElseThrow(() -> new GraphError.GraphErrorException(new GraphError("No Ontology entityType for value ", "No Ontology entityType for value[" + entityName + "]")));
        }

        /**
         * get entity by its type - returns optional with empty if not found
         *
         * @param entityName
         * @return
         */
        public Optional<String> eType(String entityName) {
            EntityType entityType = this.entitiesByName.get(entityName);
            return entityType == null ? Optional.empty() : Optional.of(entityType.geteType());
        }

        /**
         * get entity's type by its name - throws error if not found
         *
         * @param entityName
         * @return
         */
        public String eType$(String entityName) {
            return eType(entityName)
                    .orElseThrow(() -> new GraphError.GraphErrorException(new GraphError("No Ontology entityType for value ", "No Ontology entityType for value[" + entityName + "]")));
        }

        /**
         * get relation by its type - returns optional empty if not found
         *
         * @param rType
         * @return
         */
        public Optional<RelationshipType> $relation(String rType) {
            return Optional.ofNullable(this.relationsByRtype.get(rType));
        }

        /**
         * get entity/relation properties
         *
         * @param elementType
         * @return
         */
        public List<Property> $properties(BaseElement elementType) {
            return
                    elementType.getProperties().stream()
                            .map(this::pType$)
                            .collect(Collectors.toList());
        }

        /**
         * get entity properties
         *
         * @param entityType
         * @return
         */
        public List<Property> $properties(EntityType entityType) {
            return entityType.getProperties().stream()
                    .map(this::pName$)
                    .collect(Collectors.toList());
        }

        /**
         * get relation properties
         *
         * @param relationshipType
         * @return
         */
        public List<Property> $properties(RelationshipType relationshipType) {
            return relationshipType.getProperties().stream()
                    .map(this::pName$)
                    .collect(Collectors.toList());
        }

        /**
         * get relation by its type - returns error not found
         *
         * @param rType
         * @return
         */
        public RelationshipType $relation$(String rType) {
            return $relation(rType)
                    .orElseThrow(() -> new GraphError.GraphErrorException(new GraphError("No Ontology Relation for value ", "No Ontology Relation for value[" + rType + "]")));
        }

        /**
         * get relation by its relationName - returns optional empty if not found
         *
         * @param relationName
         * @return
         */
        public Optional<RelationshipType> relation(String relationName) {
            return Optional.ofNullable(this.relationsByName.get(relationName));
        }

        /**
         * get relation by its relationName - returns error if not found
         *
         * @param relationName
         * @return
         */
        public RelationshipType relation$(String relationName) {
            return relation(relationName)
                    .orElseThrow(() -> new GraphError.GraphErrorException(new GraphError("No Ontology relationName for value ", "No Ontology relationName for value[" + relationName + "]")));
        }

        /**
         * returns relation type by its name - returns optional empty if not found
         *
         * @param relationName
         * @return
         */
        public Optional<String> rType(String relationName) {
            RelationshipType relationshipType = this.relationsByName.get(relationName);
            return relationshipType == null ? Optional.empty() : Optional.of(relationshipType.getrType());
        }

        /**
         * returns relation type by its name - returns error if not found
         *
         * @param relationName
         * @return
         */
        public String rType$(String relationName) {
            return rType(relationName)
                    .orElseThrow(() -> new GraphError.GraphErrorException(new GraphError("No Ontology relationName for value ", "No Ontology relationName for value[" + relationName + "]")));
        }


        /**
         * returns property by its name - returns optional empty if not found
         *
         * @param propertyName
         * @return
         */
        public Optional<Property> pName(String propertyName) {
            return Optional.ofNullable(this.propertiesByName.get(propertyName));
        }

        /**
         * returns property by its name/type - returns optional empty if not found
         *
         * @param key
         * @return
         */
        public Optional<Property> pNameOrType(String key) {
            return Optional.ofNullable(this.propertiesByName.getOrDefault(key, this.propertiesByPtype.get(key)));
        }

        /**
         * return property (no matter which element it belongs to) - throws exception if not found
         *
         * @param propertyName
         * @return
         */
        public Property pName$(String propertyName) {
            return pName(propertyName)
                    .orElseThrow(() -> new GraphError.GraphErrorException(new GraphError("No Ontology propertyName for value ", "No Ontology propertyName for value[" + propertyName + "]")));
        }

        /**
         * get property type by name
         *
         * @param pName
         * @return
         */
        public String pTypeByName(String pName) {
            return this.propertiesByName.get(pName).getpType();
        }

        /**
         * returns property by its type - optional with value is exists and empty if not
         *
         * @param pType
         * @return
         */
        public Optional<Property> $pType(String pType) {
            return Optional.ofNullable(this.propertiesByPtype.get(pType));
        }

        /**
         * return property type (no matter which element it belongs to) - throws exception if not found
         *
         * @param pType
         * @return
         */
        public Property pType$(String pType) {
            if (!$pType(pType).isPresent())
                throw new IllegalArgumentException(String.format("No Such ontology value present %s", pType));
            return $pType(pType).get();
        }

        /**
         * return property for an element (entity/relation) directly under element or in its embedded sub elements
         *
         * @param elementType
         * @param pType
         * @return
         */
        public Optional<Property> $pType(String elementType, String pType) {
            if (cascadingFieldPType(elementType, pType).isPresent()) {
                return cascadingFieldPType(elementType, pType);
            }
            return Optional.ofNullable(this.propertiesByPtype.get(pType));
        }

        public Iterable<String> pNames() {
            return this.propertiesByName.keySet();
        }

        /**
         * returns all properties types including cascading ones and nested elements
         *
         * @return
         */
        public Iterable<String> pTypes() {
            Set<String> results = new HashSet<>();
            results.addAll(StreamSupport.stream(entities().spliterator(), false)
                    .flatMap(e -> cascadingElementFieldsPType(e.geteType()).stream())
                    .collect(Collectors.toSet()));
            results.addAll(StreamSupport.stream(relations().spliterator(), false)
                    .flatMap(e -> cascadingElementFieldsPType(e.getrType()).stream())
                    .collect(Collectors.toSet()));
            return results;
        }

        /**
         * returns all entities
         *
         * @return
         */
        public Iterable<EntityType> entities() {
            return Stream.ofAll(ontology.getEntityTypes()).toJavaList();
        }

        /**
         * check if ontology contains the next property type as metadata property
         *
         * @param pType
         * @return
         */
        public boolean containsMetadata(String pType) {
            return Stream.ofAll(ontology.entityTypes).flatMap(EntityType::getMetadata).toJavaSet().contains(pType);
        }

        /**
         * returns nested entity types that belong to a given entity
         *
         * @param eType
         * @return
         */
        public List<EntityType> nested$(String eType) {
            if (!entity(eType).isPresent()) return Collections.emptyList();
            return entity$(eType).fields().stream()
                    .map(this::pType$)
                    .filter(p -> $entity(p.getType()).isPresent())
                    .map(p -> $entity(p.getType()).get())
                    .collect(Collectors.toList());

        }
        /**
         * returns parents of nested entity type according to its relevant location in the property name - for example
         * the pType a.b.c.d with the given eType c => will return the entity b
         * the pType a.b.c.d with the given eType b => will return the entity a
         *
         * @param eType
         * @param pType
         * @return
         */
        public Optional<EntityType> nestedParent(String eType, String pType) {
            return Optional.empty();
        }

        /**
         * for a parent entity, find the nested entity that the given propertyKey belongs to
         *
         * @param parentEntity
         * @param propertyKey
         * @return
         */
        public Optional<EntityType> getNestedEntity(String parentEntity, String propertyKey) {
            if (!cascadingFieldNameOrType(parentEntity, propertyKey).isPresent())
                return Optional.empty();

            //since the field is combined of cascading entities - we take the last one which is an actual field
            String[] array = propertyKey.split("\\.");
            if (array.length < 2) return Optional.empty();

            Optional<Property> original = pName(array[array.length - 1]);
            if (!original.isPresent())
                return Optional.empty();

            return nested$(parentEntity).stream().filter(e -> e.containsProperty(original.get().getName())).findFirst();
        }

        /**
         * returns all properties
         *
         * @return
         */
        public Set<Property> properties() {
            return this.ontology.properties;
        }

        /**
         * returns the cascading field elementType of the entity/relation (a.b.field)
         *
         * @param elementType - entity/relation
         * @param nameOrType  - property cascading name or type
         * @return
         */
        public Optional<Property> cascadingFieldNameOrType(String elementType, String nameOrType) {
            if (cascadingElementFieldsPName(elementType).stream().anyMatch(p -> p.equals(nameOrType)) ||
                    cascadingElementFieldsPType(elementType).stream().anyMatch(p -> p.equals(nameOrType))) {
                //since the field is combined of cascading entities - we take the last one which is an actual field
                String[] array = nameOrType.split("\\.");
                if (array.length < 2) return Optional.empty();

                Optional<Property> original = pName(array[array.length - 1]);
                Optional<Property> eType = $pType(array[array.length - 2]);
                if (cascadingElementFieldsPName(elementType).stream().anyMatch(p -> p.equals(nameOrType)))
                    return original.map(property -> new NestedProperty(nameOrType, nameOrType, property.getType(),
                            eType.isPresent() ? eType.get().getType() : elementType));

                if (cascadingElementFieldsPType(elementType).stream().anyMatch(p -> p.equals(nameOrType)))
                    return original.map(property -> new NestedProperty(nameOrType, nameOrType, property.getType(),
                            eType.isPresent() ? eType.get().getType() : elementType));
            }
            return Optional.empty();
        }

        /**
         * returns the cascading field elementType of the entity/relation (a.b.field)
         *
         * @param elementType - entity/relation
         * @param pType       - property cascading name
         * @return
         */
        public Optional<Property> cascadingFieldPType(String elementType, String pType) {
            if (cascadingElementFieldsPType(elementType).stream().anyMatch(p -> p.equals(pType))) {
                //since the field is combined of cascading entities - we take the last one which is an actual field
                String[] array = pType.split("\\.");
                if (array.length < 2) return Optional.empty();

                Optional<Property> original = pName(array[array.length - 1]);
                Optional<Property> eType = $pType(array[array.length - 2]);
                return original.map(property -> new NestedProperty(pType, pType, property.getType(),
                        eType.isPresent() ? eType.get().getType() : elementType));
            }
            return Optional.empty();
        }

        /**
         * get all nested properties for an element (entity/relation)
         *
         * @param type
         * @return
         */
        public List<Property> generateCascadingElementFields(String type) {
            if (!$element(type).isPresent()) return Collections.emptyList();

            List<Property> propertyList = new ArrayList<>();
            //only add primitive type properties to root element
            propertyList.addAll($properties($element(type).get()).stream()
                    .filter(p -> !$element(p.getType()).isPresent()).collect(Collectors.toList()));
            //add all nested properties recursively
            propertyList.addAll(_generateCascadingElementFields(type,Optional.empty(), type));
            return propertyList;
        }

        public List<Property> _generateCascadingElementFields(String context,Optional<String> parent, String type) {
            if (!$element(type).isPresent()) return Collections.emptyList();
            return $element(type).get().fields().stream()
                    .map(this::$pType)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .flatMap(p -> _getNestedPropertyStream(context,parent, p).stream())
                    .collect(Collectors.toList());
        }

        private List<Property> _getNestedPropertyStream(String context, Optional<String> parent, Property p) {
            List<Property> props = new ArrayList<>();
            if ($element(p.getType()).isPresent()) {
                //check is this property actually an entity - if so recurse for inner nested elements
                props.add(new NestedProperty(_getPrefix(parent, p.getpType()), _getPrefix(parent, p.getpType()), p.getType(), context));
                props.addAll(_generateCascadingElementFields(p.getType(),Optional.of(_getPrefix(parent, p.getpType())), p.getType()));
            } else if (parent.isPresent()) {
                // if property not nested element - generate its primitives
                props.add(new NestedProperty(_getPrefix(parent, p.getpType()), _getPrefix(parent, p.getpType()),p.getType(), context));
            }
            return props;
        }

        private String _getPrefix(Optional<String> parent, String type) {
            return parent.map(s -> s.concat(".").concat(type)).orElse(type);
        }

        /**
         * get parent entity field name for child nested entity
         *
         * @param parentEntity
         * @param nestedEntity
         * @return
         */
        public List<Property> nestedEntityFieldName(EntityType parentEntity, EntityType nestedEntity) {
            return $properties(parentEntity).stream().filter(p -> p.getType().equals(nestedEntity.geteType())).collect(Collectors.toList());
        }

        /**
         * returns direct and nested fields (fields of nested entities/relations)
         *
         * @param type
         * @return
         */
        public List<String> cascadingElementFieldsPName(String type) {
            if (!$element(type).isPresent()) return Collections.emptyList();

            return Lists.newArrayList(Iterables.unmodifiableIterable(
                    Iterables.concat($element(type).get().fields(),
                            $element(type).get().fields().stream()
                                    .map(this::pName)
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .filter(p -> $element(p.getType()).isPresent())//check is this property actually an entity
                                    .map(p -> cascadingElementFieldsPName(p.getType()).stream()
                                            .map(e -> p.getName().concat(".").concat(e))
                                            .collect(Collectors.toList())
                                    ).flatMap(Collection::stream)
                                    .collect(Collectors.toList()))));
        }

        /**
         * returns direct and nested fields (fields of nested entities/relations)
         *
         * @param type
         * @return
         */
        public List<String> cascadingElementFieldsPType(String type) {
            if (!$element(type).isPresent()) return Collections.emptyList();

            return Lists.newArrayList(Iterables.unmodifiableIterable(
                    Iterables.concat($element(type).get().fields(),
                            $element(type).get().fields().stream()
                                    .map(this::$pType)
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .filter(p -> $element(p.getType()).isPresent())//check is this property actually an entity
                                    .map(p -> cascadingElementFieldsPType(p.getType()).stream()
                                            .map(e -> p.getName().concat(".").concat(e))
                                            .collect(Collectors.toList())
                                    ).flatMap(Collection::stream)
                                    .collect(Collectors.toList()))));
        }

        /**
         * check is the given type is a nested entity under any other entity
         *
         * @param eType
         * @return
         */
        public boolean isNestedEntity(String eType) {
            if (!entity(eType).isPresent()) return false;
            return entity$(eType).fields().stream().anyMatch(p -> $entity(p).isPresent());
        }

        /**
         * check is the given property for a given entity is a nested field and not a direct field
         *
         * @param entity
         * @param property
         * @return
         */

        public boolean isNestedField(String entity, String property) {
            if (!$element(entity).isPresent()) return false;
            return !$element(entity).get().containsProperty(property) &&
                    !$element(entity).get().containsMetadata(property) &&
                    cascadingFieldPType(entity, property).isPresent();
        }


        /**
         * returns all entities names
         *
         * @return
         */
        public Iterable<String> eNames() {
            return Stream.ofAll(entities()).map(EntityType::getName).toJavaList();
        }

        /**
         * returns all entities types
         *
         * @return
         */
        public Iterable<String> eTypes() {
            return Stream.ofAll(ontology.getEntityTypes()).map(EntityType::geteType).toJavaList();
        }

        /**
         * get all relations
         *
         * @return
         */
        public List<RelationshipType> relations() {
            return Stream.ofAll(ontology.getRelationshipTypes()).toJavaList();
        }

        /**
         * get all relations that have the source side (side A) of the given entity type
         *
         * @param eType
         * @return
         */
        public List<RelationshipType> relationBySideA(String eType) {
            return Stream.ofAll(ontology.getRelationshipTypes()).filter(r -> r.hasSideA(eType)).toJavaList();
        }

        /**
         * get all relations that have the destination side (side B) of the given entity type
         *
         * @param eType
         * @return
         */
        public List<RelationshipType> relationBySideB(String eType) {
            return Stream.ofAll(ontology.getRelationshipTypes()).filter(r -> r.hasSideB(eType)).toJavaList();
        }

        /**
         * get all relations types
         *
         * @return
         */
        public Iterable<String> rTypes() {
            return Stream.ofAll(relations()).map(RelationshipType::getrType).toJavaList();
        }

        /**
         * get all relations names
         *
         * @return
         */
        public Iterable<String> rNames() {
            return Stream.ofAll(relations()).map(RelationshipType::getName).toJavaList();
        }

        /**
         * return primitive type by its type
         *
         * @param typeName
         * @return
         */
        public Optional<PrimitiveType> primitiveType(String typeName) {
            return Stream.ofAll(ontology.getPrimitiveTypes())
                    .filter(type -> type.getType().equals(typeName))
                    .toJavaOptional();
        }

        /**
         * get primitive type by its name
         *
         * @param typeName
         * @return
         */
        public PrimitiveType primitiveType$(String typeName) {
            return primitiveType(typeName).get();
        }


        /**
         * returns all enumerations
         *
         * @return
         */
        public List<EnumeratedType> getEnumeratedTypes() {
            return ontology.getEnumeratedTypes();
        }

        public Optional<EnumeratedType> enumeratedType(String typeName) {
            return Stream.ofAll(ontology.getEnumeratedTypes())
                    .filter(type -> type.isOfType(typeName))
                    .toJavaOptional();
        }

        public EnumeratedType enumeratedType$(String typeName) {
            return enumeratedType(typeName).get();
        }
        //endregion

        //region Fields
        private Ontology ontology;

        private Map<String, EntityType> entitiesByEtype;
        private Map<String, EntityType> entitiesByName;

        private Map<String, RelationshipType> relationsByRtype;
        private Map<String, RelationshipType> relationsByName;

        private Map<String, Property> propertiesByName;
        private Map<String, Property> propertiesByPtype;

        /**
         * match named element to true type (included typed value identifier)
         *
         * @param name
         * @return
         */
        public Optional<Tuple2<NodeType, String>> matchNameToType(String name) {
            //ENUMERATED TYPE
            if (enumeratedType(name).isPresent())
                return Optional.of(Tuple.of(NodeType.ENUM, enumeratedType$(name).geteType()));
            //entity TYPE
            if (eType(name).isPresent())
                return Optional.of(Tuple.of(NodeType.ENTITY, eType$(name)));
            //relation TYPE
            if (rType(name).isPresent())
                return Optional.of(Tuple.of(NodeType.RELATION, rType$(name)));
            //property TYPE
            if (pNameOrType(name).isPresent())
                return Optional.of(Tuple.of(NodeType.PROPERTY, pNameOrType(name).get().getpType()));

            return Optional.empty();
        }


        public enum NodeType {
            ENUM, PROPERTY, RELATION, ENTITY
        }

        //endregion
    }

    public enum OntologyPrimitiveType {
        STRING,//represents keyword
        TEXT,
        DATE,
        LONG,
        INT,
        FLOAT,
        DOUBLE,
        GEO;


        public static OntologyPrimitiveType translate(String clazzName) {
            if (String.class.getName().equals(clazzName))
                return STRING;
            if (Boolean.class.getName().equals(clazzName))
                return STRING;//no special case for bool

            if (Integer.class.getName().equals(clazzName))
                return INT;

            if (Float.class.getName().equals(clazzName))
                return FLOAT;

            if (Double.class.getName().equals(clazzName))
                return DOUBLE;

            if (Long.class.getName().equals(clazzName))
                return LONG;
            if (BigDecimal.class.getName().equals(clazzName))
                return LONG;

            if (java.sql.Date.class.getName().equals(clazzName))
                return DATE;
            if (java.sql.Timestamp.class.getName().equals(clazzName))
                return DATE;
            if (Date.class.getName().equals(clazzName))
                return DATE;

            return TEXT;
        }

    }
    //endregion

}
