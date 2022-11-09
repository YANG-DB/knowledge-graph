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
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.opensearch.graph.dispatcher.ontology.IndexProviderFactory;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.model.ontology.EPair;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.RelationshipType;
import org.opensearch.graph.model.resourceInfo.GraphError;
import org.opensearch.graph.model.schema.BaseTypeElement.Type;
import org.opensearch.graph.model.schema.IndexProvider;
import org.opensearch.graph.model.schema.MappingIndexType;
import org.opensearch.graph.model.schema.Redundant;
import org.opensearch.graph.model.schema.Relation;
import org.opensearch.graph.unipop.schema.providers.*;
import org.opensearch.graph.unipop.schema.providers.indexPartitions.IndexPartitions;
import org.opensearch.graph.unipop.schema.providers.indexPartitions.NestedIndexPartitions;
import org.opensearch.graph.unipop.schema.providers.indexPartitions.StaticIndexPartitions;
import org.opensearch.graph.unipop.schema.providers.indexPartitions.TimeBasedIndexPartitions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Stream.concat;
import static org.opensearch.graph.model.GlobalConstants.ASSEMBLY;
import static org.opensearch.graph.model.schema.MappingIndexType.valueOf;
import static org.opensearch.graph.unipop.schema.providers.GraphEdgeSchema.Application.endA;
import static org.opensearch.graph.unipop.schema.providers.GraphVertexSchemaUtils.generateGraphVertexSchema;

public class GraphElementSchemaProviderJsonFactory implements GraphElementSchemaProviderFactory {

    public static final String ID = GlobalConstants.ID;
    public static final String ENTITY_A = GlobalConstants.EdgeSchema.SOURCE;
    public static final String ENTITY_A_ID = GlobalConstants.EdgeSchema.SOURCE_ID;
    public static final String ENTITY_B = GlobalConstants.EdgeSchema.DEST;
    public static final String ENTITY_B_ID = GlobalConstants.EdgeSchema.DEST_ID;
    public static final String DIRECTION = GlobalConstants.EdgeSchema.DIRECTION;
    public static final String OUT = GlobalConstants.EdgeSchema.DIRECTION_OUT;
    public static final String IN = GlobalConstants.EdgeSchema.DIRECTION_IN;

    private IndexProvider indexProvider;
    private Ontology.Accessor accessor;

    @Inject
    public GraphElementSchemaProviderJsonFactory(Config config, IndexProviderFactory indexProvider, OntologyProvider ontologyProvider) {
        String assembly = config.getString(ASSEMBLY);

        this.accessor = new Ontology.Accessor(ontologyProvider.get(assembly).orElseThrow(() ->
                new GraphError.GraphErrorException(new GraphError("No Ontology present for assembly", "No Ontology present for assembly" + assembly))));

        //if no index provider found with assembly name - generate default one according to ontology and simple Static Index Partitioning strategy
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
        MappingIndexType type = valueOf(r.getPartition().toUpperCase());
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
                .flatMap(e -> generateGraphVertexSchema(accessor, indexProvider, e).stream())
                .collect(Collectors.toList());
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
        redundantPropertySchemas.add(new GraphRedundantPropertySchema.Impl(ID, String.format("%s.%s", entitySide, ID), "text"));
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
}
