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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.opensearch.graph.model.schema.BaseTypeElement;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.opensearch.graph.unipop.schema.providers.GraphEdgeSchema.Application.endB;

public interface GraphElementSchemaProvider {
    Iterable<GraphVertexSchema> getVertexSchemas(String label);

    Iterable<GraphEdgeSchema> getEdgeSchemas(String label);
    Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, String label);
    Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, Direction direction, String label);
    Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, Direction direction, String label, String vertexLabelB);

    Optional<GraphElementPropertySchema> getPropertySchema(String name, String contextEntity);

    Iterable<String> getVertexLabels();
    Iterable<String> getEdgeLabels();
    Iterable<String> getPropertyNames();
    Iterable<String> getPropertyPTypes();
    Optional<String> getLabelFieldName();

    default Iterable<GraphVertexSchema> getVertexSchemas() {
        return Stream.ofAll(getVertexLabels())
                .flatMap(this::getVertexSchemas)
                .toSet();
    }

    default Iterable<GraphEdgeSchema> getEdgeSchemas() {
        return Stream.ofAll(getEdgeLabels())
                .flatMap(this::getEdgeSchemas)
                .toSet();
    }

    default Iterable<GraphElementPropertySchema> getVertexPropertySchemas() {
        return StreamSupport.stream(getVertexSchemas().spliterator(),false)
                .flatMap(v->StreamSupport.stream(v.getProperties().spliterator(),false))
                .collect(Collectors.toSet());
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    class Impl implements GraphElementSchemaProvider {
        //region Constructors
        public Impl(Iterable<GraphVertexSchema> vertexSchemas,
                    Iterable<GraphEdgeSchema> edgeSchemas) {
            this(vertexSchemas, edgeSchemas, Collections.emptyList());
        }

        public Impl(Optional<String> labelFieldName,
                Iterable<GraphVertexSchema> vertexSchemas,
                    Iterable<GraphEdgeSchema> edgeSchemas) {
            this(labelFieldName,vertexSchemas, edgeSchemas, Collections.emptyList());
        }

        public Impl(Iterable<GraphVertexSchema> vertexSchemas,
                    Iterable<GraphEdgeSchema> edgeSchemas,
                    Iterable<GraphElementPropertySchema> propertyNameSchemas) {
            this(null,vertexSchemas,edgeSchemas, propertyNameSchemas);
        }

        public Impl(Optional<String> labelFieldName,
                Iterable<GraphVertexSchema> vertexSchemas,
                    Iterable<GraphEdgeSchema> edgeSchemas,
                    Iterable<GraphElementPropertySchema> propertyNameSchemas) {
            this.labelFieldName = labelFieldName;
            this.vertexSchemas = Stream.ofAll(vertexSchemas)
                    .groupBy(GraphElementSchema::getLabel)
                    .toJavaMap(grouping -> new Tuple2<>(grouping._1().getName(), grouping._2().toJavaList()));

            this.edgeSchemas = complementEdgeSchemas(edgeSchemas);

            complementPropertiesSchema(vertexSchemas);

            this.vertexLabels =
                    Stream.ofAll(this.vertexSchemas.values())
                    .flatMap(vertexSchemas1 -> vertexSchemas1)
                    .map(GraphElementSchema::getLabel)
                    .map(BaseTypeElement.Type::getName)
                    .toJavaSet();

            this.edgeLabels =
                    Stream.ofAll(this.edgeSchemas.values())
                            .flatMap(edgeSchemas1 -> edgeSchemas1)
                            .map(GraphElementSchema::getLabel)
                            .map(BaseTypeElement.Type::getName)
                            .toJavaSet();

        }

        /**
         * unify properties schema
         *
         * @param vertexSchemas
         * @return
         */
        private List<GraphElementPropertySchema> complementPropertiesSchema(Iterable<GraphVertexSchema> vertexSchemas) {
            return StreamSupport.stream(vertexSchemas.spliterator(),false)
                    .flatMap(schema->resolveNestedProperties(schema).stream())
                    .collect(Collectors.toList());
        }

        private List<GraphElementPropertySchema> resolveNestedProperties(GraphVertexSchema vertexSchemas) {
            vertexSchemas.getNestedSchemas().forEach(nested->
                nested.getProperties().forEach(np->
                    this.getPropertySchema(np.getpType(), nested.getLabel().getName())
                            .ifPresent(p->p.addIndexSchemas(np.getIndexingSchemes()))
                )
            );
            return StreamSupport.stream(vertexSchemas.getProperties().spliterator(),false).collect(Collectors.toList());
        }
        //endregion

        //region GraphElementSchemaProvider Implementation
        @Override
        public Iterable<GraphVertexSchema> getVertexSchemas(String label) {
            return Optional.ofNullable(this.vertexSchemas.get(label)).orElseGet(Collections::emptyList);
        }

        @Override
        public Iterable<GraphEdgeSchema> getEdgeSchemas(String label) {
            return Optional.ofNullable(this.edgeSchemas.get(edgeSchemaKey(label))).orElseGet(Collections::emptyList);
        }

        @Override
        public Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, String label) {
            return Optional.ofNullable(this.edgeSchemas.get(edgeSchemaKey(vertexLabelA, label))).orElseGet(Collections::emptyList);
        }

        @Override
        public Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, Direction direction, String label) {
            //this.edgeSchemas.entrySet().stream().filter(p->!p.getValue().isEmpty()).filter(p->p.getKey().contains("relatedEntity")).collect(Collectors.toList())
            switch (direction) {
                case OUT:
                case IN:
                    return Optional.ofNullable(this.edgeSchemas.get(edgeSchemaKey(vertexLabelA, direction.toString(), label)))
                            .orElseGet(Collections::emptyList);
                default://both - first search the out going direction if no found try the in direction
                    return Optional.ofNullable(this.edgeSchemas.get(edgeSchemaKey(vertexLabelA, Direction.OUT.toString(), label)))
                            .orElseGet(()->this.edgeSchemas.get(edgeSchemaKey(vertexLabelA, Direction.IN.toString(), label)));
            }
        }

        @Override
        public Iterable<GraphEdgeSchema> getEdgeSchemas(String vertexLabelA, Direction direction, String label, String vertexLabelB) {
            switch (direction) {
                case OUT:
                case IN:
                    return Optional.ofNullable(this.edgeSchemas.get(edgeSchemaKey(vertexLabelA, direction.toString(), label,vertexLabelB)))
                            .orElseGet(Collections::emptyList);
                default://both - first search the out going direction if no found try the in direction
                    return Optional.ofNullable(this.edgeSchemas.get(edgeSchemaKey(vertexLabelA, Direction.OUT.toString(), label,vertexLabelB)))
                            .orElseGet(()->this.edgeSchemas.get(edgeSchemaKey(vertexLabelA, Direction.IN.toString(), label,vertexLabelB)));
            }
        }

        @Override
        public Optional<GraphElementPropertySchema> getPropertySchema(String fieldName, String contextEntity) {
            if(vertexSchemas.containsKey(contextEntity)) {
                return vertexSchemas.get(contextEntity).stream()
                        .flatMap(e->StreamSupport.stream(e.getProperties().spliterator(),false))
                        .filter(p->p.getName().equals(fieldName))
                        .findFirst();
            }
            if(edgeSchemas.containsKey(contextEntity)) {
                return edgeSchemas.get(contextEntity).stream()
                        .flatMap(e->StreamSupport.stream(e.getProperties().spliterator(),false))
                        .filter(p->p.getName().equals(fieldName))
                        .findFirst();
            }

            return Optional.empty();
        }

        @Override
        public Iterable<String> getVertexLabels() {
            return this.vertexLabels;
        }

        @Override
        public Iterable<String> getEdgeLabels() {
            return this.edgeLabels;
        }

        @Override
        public Iterable<String> getPropertyNames() {
            return this.propertyNames;
        }
        @Override
        public Iterable<String> getPropertyPTypes() {
            return this.propertyPTypes;
        }

        @Override
        public Optional<String> getLabelFieldName() {
            return Objects.isNull(labelFieldName) ? Optional.empty() : labelFieldName;
        }
        //endregion

        //region

        //region Protected Methods
        protected Map<String, List<GraphEdgeSchema>> complementEdgeSchemas(Iterable<GraphEdgeSchema> edgeSchemas) {
            Iterable<GraphEdgeSchema> complementedSchemas =
                    Stream.ofAll(edgeSchemas)
                    .flatMap(edgeSchema -> complementEdgeSchema(edgeSchema))
                    .toJavaList();

            Map<String, List<GraphEdgeSchema>> labelSchemas =
                    Stream.ofAll(edgeSchemas) // temporary - should be complementedSchemas
                            .groupBy(edgeSchema -> edgeSchemaKey(edgeSchema.getLabel().getName()))
                            .toJavaMap(grouping -> new Tuple2<>(grouping._1(), grouping._2().toJavaList()));

            Map<String, List<GraphEdgeSchema>> labelALabelSchemas =
                    Stream.ofAll(complementedSchemas)
                            .filter(edgeSchema -> edgeSchema.getEndA().isPresent())
                            .filter(edgeSchema -> edgeSchema.getEndA().get().getLabel().isPresent())
                            .groupBy(edgeSchema -> edgeSchemaKey(edgeSchema.getEndA().get().getLabel().get(),
                                    edgeSchema.getLabel().getName()))
                            .toJavaMap(grouping -> new Tuple2<>(grouping._1(), grouping._2().toJavaList()));

            Map<String, List<GraphEdgeSchema>> labelADirLabelSchemas =
                    Stream.ofAll(complementedSchemas)
                            .filter(edgeSchema -> edgeSchema.getEndA().isPresent())
                            .filter(edgeSchema -> edgeSchema.getEndA().get().getLabel().isPresent())
                            .groupBy(edgeSchema -> edgeSchemaKey(
                                    edgeSchema.getEndA().get().getLabel().get(),
                                    edgeSchema.getDirection().toString(),
                                    edgeSchema.getLabel().getName()))
                            .toJavaMap(grouping -> new Tuple2<>(grouping._1(), grouping._2().toJavaList()));

            Map<String, List<GraphEdgeSchema>> labelADirLabelLabelBSchemas =
                    Stream.ofAll(complementedSchemas)
                            .filter(edgeSchema -> edgeSchema.getEndA().isPresent())
                            .filter(edgeSchema -> edgeSchema.getEndA().get().getLabel().isPresent())
                            .filter(edgeSchema -> edgeSchema.getEndB().isPresent())
                            .filter(edgeSchema -> edgeSchema.getEndB().get().getLabel().isPresent())
                            .groupBy(edgeSchema -> edgeSchemaKey(
                                    edgeSchema.getEndA().get().getLabel().get(),
                                    edgeSchema.getDirection().toString(),
                                    edgeSchema.getLabel().getName(),
                                    edgeSchema.getEndB().get().getLabel().get()))
                            .toJavaMap(grouping -> new Tuple2<>(grouping._1(), grouping._2().toJavaList()));

            Map<String, List<GraphEdgeSchema>> complementedSchemaMap = new HashMap<>();
            complementedSchemaMap.putAll(labelSchemas);
            complementedSchemaMap.putAll(labelALabelSchemas);
            complementedSchemaMap.putAll(labelADirLabelSchemas);
            complementedSchemaMap.putAll(labelADirLabelLabelBSchemas);
            return complementedSchemaMap;
        }

        protected Iterable<GraphEdgeSchema> complementEdgeSchema(GraphEdgeSchema edgeSchema) {
            List<GraphEdgeSchema> edgeSchemas = new ArrayList<>();
            if (edgeSchema.getApplications().contains(GraphEdgeSchema.Application.endA)) {
                edgeSchemas.add(new GraphEdgeSchema.Impl(
                        edgeSchema.getLabel(),
                        edgeSchema.getConstraint(),
                        edgeSchema.getEndA(),
                        edgeSchema.getEndB(),
                        edgeSchema.getDirection(),
                        edgeSchema.getDirectionSchema(),
                        edgeSchema.getRouting(),
                        edgeSchema.getIndexPartitions(),
                        edgeSchema.getProperties(),
                        edgeSchema.getApplications()
                ));
            }

            if (edgeSchema.getApplications().contains(endB)) {
                edgeSchemas.add(new GraphEdgeSchema.Impl(
                        edgeSchema.getLabel(),
                        edgeSchema.getConstraint(),
                        edgeSchema.getEndB(),
                        edgeSchema.getEndA(),
                        edgeSchema.getDirection().equals(Direction.OUT) ? Direction.IN : Direction.OUT,
                        edgeSchema.getDirectionSchema(),
                        edgeSchema.getRouting(),
                        edgeSchema.getIndexPartitions(),
                        edgeSchema.getProperties(),
                        edgeSchema.getApplications()
                ));
            }

            return edgeSchemas;
        }

        protected String edgeSchemaKey(String...parts) {
            return String.join(".", Stream.of(parts));
        }
        //endregion

        //region Fields
        protected Map<String, List<GraphVertexSchema>> vertexSchemas;
        protected Map<String, List<GraphEdgeSchema>> edgeSchemas;
        protected Iterable<String> vertexLabels;
        protected Iterable<String> edgeLabels;
        protected Iterable<String> propertyNames;
        protected Iterable<String> propertyPTypes;
        protected Optional<String> labelFieldName;
        //endregion
    }

    class Cached extends GraphElementSchemaProvider.Impl {
        //region Constructors
        public Cached(GraphElementSchemaProvider schemaProvider) {
            super(Collections.emptyList(), Collections.emptyList());

            this.schemaProvider = schemaProvider;
            this.labelFieldName = this.schemaProvider.getLabelFieldName();
            this.vertexLabels = this.schemaProvider.getVertexLabels();
            this.edgeLabels = this.schemaProvider.getEdgeLabels();
            this.propertyNames = this.schemaProvider.getPropertyNames();

            initializeVertexSchemas();
            initializeEdgeSchemas();
        }
        //endregion

        //region Private Methods
        private void initializeVertexSchemas() {
            this.vertexSchemas = new HashMap<>();

            for(String vertexLabel : this.vertexLabels) {
                this.vertexSchemas.put(vertexLabel, Stream.ofAll(this.schemaProvider.getVertexSchemas(vertexLabel)).toJavaList());
            }
        }

        private void initializeEdgeSchemas() {
            this.edgeSchemas = new HashMap<>();

            for(String edgeLabel : this.edgeLabels) {
                this.edgeSchemas.put(this.edgeSchemaKey(edgeLabel),
                        Stream.ofAll(this.schemaProvider.getEdgeSchemas(edgeLabel)).toJavaList());

                for(String vertexLabelA : this.vertexLabels) {
                    this.edgeSchemas.put(this.edgeSchemaKey(vertexLabelA, edgeLabel),
                            Stream.ofAll(this.schemaProvider.getEdgeSchemas(vertexLabelA, edgeLabel)).toJavaList());

                    for(Direction direction : Arrays.asList(Direction.OUT, Direction.IN)) {
                        this.edgeSchemas.put(this.edgeSchemaKey(vertexLabelA, direction.toString(), edgeLabel),
                                Stream.ofAll(this.schemaProvider.getEdgeSchemas(vertexLabelA, direction, edgeLabel)).toJavaList());

                        for(String vertexLabelB : this.vertexLabels) {
                            this.edgeSchemas.put(this.edgeSchemaKey(vertexLabelA, direction.toString(), edgeLabel, vertexLabelB),
                                    Stream.ofAll(this.schemaProvider.getEdgeSchemas(vertexLabelA, direction, edgeLabel, vertexLabelB)).toJavaList());
                        }
                    }
                }
            }
        }
        //endregion

        //region Fields
        private GraphElementSchemaProvider schemaProvider;
        //endregion
    }
}
