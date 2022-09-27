package org.opensearch.graph.executor.ontology.schema;

/*-
 * #%L
 * virtual-core
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


import com.google.inject.Inject;
import com.typesafe.config.Config;
import org.opensearch.graph.dispatcher.ontology.IndexProviderFactory;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.model.ontology.*;
import org.opensearch.graph.model.schema.BaseTypeElement.Type;
import org.opensearch.graph.model.schema.MappingIndexType;
import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.model.resourceInfo.GraphError;
import org.opensearch.graph.model.schema.*;
import org.opensearch.graph.unipop.schemaProviders.*;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.IndexPartitions;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.NestedIndexPartitions;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.TimeSeriesIndexPartitions;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.opensearch.graph.unipop.schemaProviders.GraphEdgeSchema.Application.endA;
import static java.util.stream.Stream.concat;

public class GraphElementSchemaProviderJsonFactory implements GraphElementSchemaProviderFactory {

    public static final String KEYWORD = "keyword";
    public static final String TEXT = "text";
    public static final String _ID = GlobalConstants._ID;

    public static final String ID = "id";
    public static final String ENTITY_A = GlobalConstants.EdgeSchema.SOURCE;
    public static final String ENTITY_A_ID = GlobalConstants.EdgeSchema.SOURCE_ID;
    public static final String ENTITY_B = GlobalConstants.EdgeSchema.DEST;
    public static final String ENTITY_B_ID = GlobalConstants.EdgeSchema.DEST_ID;
    public static final String DIRECTION = GlobalConstants.EdgeSchema.DIRECTION;
    public static final String OUT = "out";
    public static final String IN = "in";

    private IndexProvider indexProvider;
    private Ontology.Accessor accessor;

    @Inject
    public GraphElementSchemaProviderJsonFactory(Config config, IndexProviderFactory indexProvider, OntologyProvider ontologyProvider) {
        String assembly = config.getString("assembly");

        this.accessor = new Ontology.Accessor(ontologyProvider.get(assembly).orElseThrow(() ->
                new GraphError.GraphErrorException(new GraphError("No Ontology present for assembly", "No Ontology present for assembly" + assembly))));

        //if no index provider found with assembly name - generate default one accoring to ontology and simple Static Index Partitioning strategy
        this.indexProvider = indexProvider.get(assembly).orElseGet(() ->
                IndexProvider.Builder.generate(accessor.get()));

    }

    public GraphElementSchemaProviderJsonFactory(IndexProviderFactory indexProviderFactory, Ontology ontology) {
        this.accessor = new Ontology.Accessor(ontology);
        this.indexProvider = indexProviderFactory.get(ontology.getOnt())
                .orElseGet(() -> IndexProvider.Builder.generate(ontology));
    }

    public GraphElementSchemaProviderJsonFactory(IndexProvider indexProvider, Ontology ontology) {
        this.accessor = new Ontology.Accessor(ontology);
        this.indexProvider = indexProvider;
    }

    @Override
    public GraphElementSchemaProvider get(Ontology ontology) {
        return new GraphElementSchemaProvider.Impl(
                getVertexSchemas(),
                getEdgeSchemas());
    }

    private List<GraphEdgeSchema> getEdgeSchemas() {
        return indexProvider.getRelations().stream()
                .flatMap(r -> generateGraphEdgeSchema(r).stream())
                .collect(Collectors.toList());
    }

    private List<GraphEdgeSchema> generateGraphEdgeSchema(Relation r) {
        MappingIndexType type = MappingIndexType.valueOf(r.getPartition().toUpperCase());
        switch (type) {
            case UNIFIED:
                //todo verify correctness
                return r.getProps().getValues().stream()
                        .flatMap(v -> generateGraphEdgeSchema(r, r.getType(), new StaticIndexPartitions(v)).stream())
                        .collect(Collectors.toList());
            case STATIC:
                return r.getProps().getValues().stream()
                        .flatMap(v -> generateGraphEdgeSchema(r, r.getType(), new StaticIndexPartitions(v)).stream())
                        .collect(Collectors.toList());
            case NESTED:
                return r.getProps().getValues().stream()
                        .flatMap(v -> generateGraphEdgeSchema(r, r.getType(), new NestedIndexPartitions(v)).stream())
                        .collect(Collectors.toList());
            case TIME:
                return generateGraphEdgeSchema(r, r.getType(), new TimeBasedIndexPartitions(r.getProps()));
        }

        return Collections.singletonList(new GraphEdgeSchema.Impl(r.getType(),
                new StaticIndexPartitions(r.getProps().getValues().isEmpty() ? r.getType().getName() : r.getProps().getValues().get(0))));
    }


    private List<GraphVertexSchema> getVertexSchemas() {
        return indexProvider.getEntities().stream()
                .flatMap(e -> generateGraphVertexSchema(e).stream())
                .collect(Collectors.toList());
    }

    private List<GraphVertexSchema> generateGraphVertexSchema(Entity e) {
        MappingIndexType type = MappingIndexType.valueOf(e.getPartition().toUpperCase());
        switch (type) {
            case UNIFIED:
            case STATIC:
                //todo verify correctness
                return e.getProps().getValues().stream()
                        .map(v -> new GraphVertexSchema.Impl(
                                e.getType(),
                                new StaticIndexPartitions(v),
                                getGraphElementPropertySchemas(e.getType().getName())))
                        .collect(Collectors.toList());
            case NESTED:
                return e.getProps().getValues().stream()
                        .map(v -> new GraphVertexSchema.Impl(
                                e.getType(),
                                new NestedIndexPartitions(v),
                                getGraphElementPropertySchemas(e.getType().getName())))
                        .collect(Collectors.toList());
            case TIME:
                return e.getProps().getValues().stream()
                        .map(v -> new GraphVertexSchema.Impl(
                                e.getType(),
                                new TimeBasedIndexPartitions(e.getProps()),
                                getGraphElementPropertySchemas(e.getType().getName())))
                        .collect(Collectors.toList());
        }
        //default - when other partition type is declared
        String v = e.getProps().getValues().isEmpty() ? e.getType().getName() : e.getProps().getValues().get(0);
        return Collections.singletonList(
                new GraphVertexSchema.Impl(
                        e.getType(),
                        new StaticIndexPartitions(v),
                        getGraphElementPropertySchemas(e.getType().getName())));
    }


    private Optional<List<EPair>> getEdgeSchemaOntologyPairs(String edge) {
        Optional<RelationshipType> relation = accessor.relation(edge);
        return relation.map(RelationshipType::getePairs);
    }

    private List<GraphEdgeSchema> generateGraphEdgeSchema(Relation r, Type v, IndexPartitions partitions) {
        Optional<List<EPair>> pairs = getEdgeSchemaOntologyPairs(v.getName());

        if (!pairs.isPresent())
            throw new GraphError.GraphErrorException(new GraphError("Schema generation exception", "No edges pairs are found for given relation name " + v));

        List<EPair> pairList = pairs.get();
        validateSchema(pairList);

        return concat(
                pairList.stream().map(p -> constructEdgeSchema(r, v, partitions, p, Direction.OUT)),
                pairList.stream().map(p -> constructEdgeSchema(r, v, partitions, p, Direction.IN)))
                .collect(Collectors.toList());
    }

    private GraphEdgeSchema.Impl constructEdgeSchema(Relation r, Type v, IndexPartitions partitions, EPair p, Direction direction) {
        switch (direction) {
            case IN:
                return new GraphEdgeSchema.Impl(
                        v,
                        new GraphElementConstraint.Impl(__.has(T.label, v.getName())),
                        Optional.of(new GraphEdgeSchema.End.Impl(
                                Collections.singletonList(ENTITY_A_ID),
                                Optional.of(p.geteTypeB()),
                                getGraphRedundantPropertySchemas(ENTITY_B, p.geteTypeB(), r))),
                        Optional.of(new GraphEdgeSchema.End.Impl(
                                Collections.singletonList(ENTITY_B_ID),
                                Optional.of(p.geteTypeA()),
                                getGraphRedundantPropertySchemas(ENTITY_A, p.geteTypeA(), r))),
                        direction,
                        Optional.of(new GraphEdgeSchema.DirectionSchema.Impl(DIRECTION, OUT, IN)),
                        Optional.empty(),
                        Optional.of(partitions),
                        Collections.emptyList(),
                        Stream.of(endA).toJavaSet());
            //Also IN case:
            default:
                return new GraphEdgeSchema.Impl(
                        v,
                        new GraphElementConstraint.Impl(__.has(T.label, v.getName())),
                        Optional.of(new GraphEdgeSchema.End.Impl(
                                Collections.singletonList(ENTITY_A_ID),
                                Optional.of(p.geteTypeA()),
                                getGraphRedundantPropertySchemas(ENTITY_A, p.geteTypeA(), r))),
                        Optional.of(new GraphEdgeSchema.End.Impl(
                                Collections.singletonList(ENTITY_B_ID),
                                Optional.of(p.geteTypeB()),
                                getGraphRedundantPropertySchemas(ENTITY_B, p.geteTypeB(), r))),
                        direction,
                        Optional.of(new GraphEdgeSchema.DirectionSchema.Impl(DIRECTION, OUT, IN)),
                        Optional.empty(),
                        Optional.of(partitions),
                        Collections.emptyList(),
                        Stream.of(endA).toJavaSet());

        }

    }

    private void validateSchema(List<EPair> pairList) {
        pairList.forEach(pair -> {
            if (!(accessor.$element(pair.geteTypeA()).isPresent()) || !(accessor.$element(pair.geteTypeB()).isPresent()))
                throw new GraphError.GraphErrorException(new GraphError("Schema generation exception", " Pair containing " + pair.toString() + " was not matched against the current ontology"));
        });
    }

    private List<GraphElementPropertySchema> getGraphElementPropertySchemas(String type) {
        List<GraphError> schemaValidationErrors = new ArrayList<>();
        Optional<EntityType> entity = accessor.entity(type);
        if (!entity.isPresent())
            throw new GraphError.GraphErrorException(new GraphError(String.format("No Schema element found for %s", type),
                    "Error Creating Index Schema"));

        EntityType entityType = entity.get();
        List<GraphElementPropertySchema> elementPropertySchemas = new ArrayList<>();
        accessor.cascadingElementFieldsPType(entityType.geteType())
                .forEach(v -> {
                    Optional<Property> prop = accessor.$pType(entityType.geteType(), v);
                    if (!prop.isPresent()) {
                        schemaValidationErrors.add(new GraphError(String.format("No Schema element found for %s", v), "Error Creating Index Schema"));
                    } else {
                        Property property = prop.get();
                        switch (property.getType()) {
                            case TEXT:
                                elementPropertySchemas.add(new GraphElementPropertySchema.Impl(v, property.getType(),
                                        //todo add all types of possible analyzers - such as ngram ...
                                        Arrays.asList(new GraphElementPropertySchema.ExactIndexingSchema.Impl(v + "." + KEYWORD))));
                                break;
                            default:
                                elementPropertySchemas.add(new GraphElementPropertySchema.Impl(v, property.getType()));
                        }
                    }
                });

        if (schemaValidationErrors.isEmpty())
            return elementPropertySchemas;

        throw new GraphError.GraphErrorException(schemaValidationErrors);
    }


    private List<GraphRedundantPropertySchema> getGraphRedundantPropertySchemas(String entitySide, String entityType, Relation rel) {
        List<GraphRedundantPropertySchema> redundantPropertySchemas = new ArrayList<>();
        //verify ontology
        accessor.$element(entityType)
                .orElseThrow(() -> new GraphError.GraphErrorException(new GraphError("Schema generation exception", "No Element in Ontology " + entityType)))
                .getIdField()
                .forEach(field -> {
                    if (!accessor.$element(entityType).get().fields().contains(field))
                        throw new GraphError.GraphErrorException(new GraphError("Schema generation exception", " Element " + entityType + " not containing " + ID + " metadata property "));
                });
        validateRedundant(entityType, entitySide, rel.getRedundant());
        redundantPropertySchemas.add(new GraphRedundantPropertySchema.Impl(ID, String.format("%s.%s", entitySide, ID), "string"));
        //add all RedundantProperty according to schema
        validateRedundant(entityType, entitySide, rel.getRedundant());
        rel.getRedundant()
                .stream()
                .filter(r -> r.getSide().contains(entitySide))
                .forEach(r -> {
                    redundantPropertySchemas.add(new GraphRedundantPropertySchema.Impl(r.getName(), String.format("%s.%s", entitySide, r.getName()), r.getType()));
                });
        return redundantPropertySchemas;
    }

    private void validateRedundant(String entityType, String entitySide, List<Redundant> redundant) {
        redundant.stream()
                .filter(r -> r.getSide().contains(entitySide))
                .forEach(r -> {
                    if (!accessor.entity(entityType).get().fields().contains(r.getName()))
                        throw new GraphError.GraphErrorException(new GraphError("Schema generation exception", " Entity " + entityType + " not containing " + r.getName() + " property (as redundant ) "));
                });
    }

    /**
     * new GraphEdgeSchema.Impl(
     * "fire",
     * new GraphElementConstraint.Impl(__.has(T.label, "fire")),
     * Optional.of(new GraphEdgeSchema.End.Impl(
     * Collections.singletonList(GlobalConstants.EdgeSchema.SOURCE_ID),
     * Optional.of("Dragon"),
     * Arrays.asList(
     * new GraphRedundantPropertySchema.Impl("id", GlobalConstants.EdgeSchema.DEST_ID, "string"),
     * new GraphRedundantPropertySchema.Impl("type", GlobalConstants.EdgeSchema.DEST_TYPE, "string")
     * ))),
     * Optional.of(new GraphEdgeSchema.End.Impl(
     * Collections.singletonList(GlobalConstants.EdgeSchema.DEST_ID),
     * Optional.of("Dragon"),
     * Arrays.asList(
     * new GraphRedundantPropertySchema.Impl("id", GlobalConstants.EdgeSchema.DEST_ID, "string"),
     * new GraphRedundantPropertySchema.Impl("type", GlobalConstants.EdgeSchema.DEST_TYPE, "string")
     * ))),
     * Direction.OUT,
     * Optional.of(new GraphEdgeSchema.DirectionSchema.Impl(GlobalConstants.EdgeSchema.DIRECTION, "out", "in")),
     * Optional.empty(),
     * Optional.of(new StaticIndexPartitions(Collections.singletonList(FIRE.getName().toLowerCase()))),
     * Collections.emptyList(),
     * Stream.of(endA).toJavaSet())
     */

    public static class TimeBasedIndexPartitions implements TimeSeriesIndexPartitions {
        private Props props;
        private SimpleDateFormat dateFormat;

        TimeBasedIndexPartitions(Props props) {
            this.props = props;
            this.dateFormat = new SimpleDateFormat(getDateFormat());
        }


        @Override
        public String getDateFormat() {
            return props.getDateFormat();
        }

        @Override
        public String getIndexPrefix() {
            return props.getPrefix();
        }

        @Override
        public String getIndexFormat() {
            return props.getIndexFormat();
        }

        @Override
        public String getTimeField() {
            return props.getPartitionField();
        }

        @Override
        public String getIndexName(Date date) {
            String format = String.format(getIndexFormat(), dateFormat.format(date));
            List<String> indices = Stream.ofAll(getPartitions())
                    .flatMap(Partition::getIndices)
                    .filter(index -> index.equals(format))
                    .toJavaList();

            return indices.isEmpty() ? null : indices.get(0);
        }

        @Override
        public Optional<String> getPartitionField() {
            return Optional.of(getTimeField());
        }

        @Override
        public Iterable<Partition> getPartitions() {
            return Collections.singletonList(() -> Stream.ofAll(props.getValues())
                    .map(p -> String.format(getIndexFormat(), p))
                    .distinct().sorted()
                    .toJavaList());
        }
    }


}
