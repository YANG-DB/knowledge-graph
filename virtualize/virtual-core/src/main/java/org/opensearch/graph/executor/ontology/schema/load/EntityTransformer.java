package org.opensearch.graph.executor.ontology.schema.load;

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





import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import org.opensearch.graph.dispatcher.driver.IdGeneratorDriver;
import org.opensearch.graph.dispatcher.ontology.IndexProviderFactory;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.executor.ontology.DataTransformer;
import org.opensearch.graph.executor.ontology.schema.RawSchema;
import org.opensearch.graph.executor.opensearch.EngineIndexProviderMappingFactory;
import org.opensearch.graph.model.Range;
import org.opensearch.graph.model.logical.LogicalEdge;
import org.opensearch.graph.model.logical.LogicalGraphModel;
import org.opensearch.graph.model.logical.LogicalNode;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.resourceInfo.GraphError;
import org.opensearch.graph.model.schema.Entity;
import org.opensearch.graph.model.schema.IndexProvider;
import org.opensearch.graph.model.schema.Redundant;
import org.opensearch.graph.model.schema.Relation;
import javaslang.Tuple2;
import org.opensearch.client.Client;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.opensearch.graph.executor.ontology.schema.load.DataLoaderUtils.parseValue;

public class EntityTransformer implements DataTransformer<DataTransformerContext<LogicalGraphModel>, LogicalGraphModel> {


    private final Ontology.Accessor accessor;
    private IndexProvider indexProvider;
    private final RawSchema schema;
    private final IdGeneratorDriver<Range> idGenerator;
    private final Client client;
    private final ObjectMapper mapper;

    @Inject
    public EntityTransformer(Config config, OntologyProvider ontology, IndexProviderFactory indexProvider, RawSchema schema, IdGeneratorDriver<Range> idGenerator, Client client) {
        String assembly = config.getString("assembly");
        this.accessor = new Ontology.Accessor(ontology.get(assembly).orElseThrow(
                () -> new GraphError.GraphErrorException(new GraphError("No Ontology present for assembly", "No Ontology present for assembly" + assembly))));
        //if no index provider found with assembly name - generate default one accoring to ontology and simple Static Index Partitioning strategy
        this.indexProvider = indexProvider.get(assembly).orElseGet(() -> IndexProvider.Builder.generate(accessor.get()));
        this.schema = schema;
        this.idGenerator = idGenerator;
        this.client = client;
        this.mapper = new ObjectMapper();
    }

    @Override
    public DataTransformerContext<LogicalGraphModel> transform(LogicalGraphModel graph, GraphDataLoader.Directive directive) {
        DataTransformerContext<LogicalGraphModel> context = new DataTransformerContext<>(mapper);
        context.withContainer(graph);
        context.withEntities(graph.getNodes().stream().map(n -> translate(context, n)).collect(Collectors.toList()));
        //out direction
        context.withRelations(graph.getEdges().stream().map(e -> translate(context, e, "out")).collect(Collectors.toList()));
        //in direction
        context.withRelations(graph.getEdges().stream().map(e -> translate(context, e, "in")).collect(Collectors.toList()));
        return context;
    }

    /**
     * translate edge to document
     *
     * @param context
     * @param edge
     * @param direction
     * @return
     */
    private DocumentBuilder translate(DataTransformerContext<LogicalGraphModel> context, LogicalEdge edge, String direction) {
        try {
            ObjectNode element = mapper.createObjectNode();
            Relation relation = indexProvider.getRelation(edge.label())
                    .orElseThrow(() -> new GraphError.GraphErrorException(new GraphError("Logical Graph Transformation Error", "No matching edge found with label " + edge.label())));
            //put classifiers
            String id = String.format("%s.%s", edge.getId(), direction);
            element.put(EngineIndexProviderMappingFactory.ID, id);
            element.put(EngineIndexProviderMappingFactory.TYPE, relation.getType());
            element.put(EngineIndexProviderMappingFactory.DIRECTION, direction);

            //populate metadata
            populateMetadataFields(mapper, indexProvider, accessor, context, edge, element);
            //populate fields
            populateFields(context, edge, relation, direction, element);

            //partition field in case of none static partitioning index
            Optional<Tuple2<String, String>> partition = Optional.empty();

            //in case of a partition field - set in the document builder
            String field = relation.getProps().getPartitionField();
            if (field != null)
                partition = Optional.of(new Tuple2<>(field, parseValue(accessor.property$(field).getType(), edge.getProperty(field), Utils.sdf).toString()));

            return new DocumentBuilder(element, id, relation.getType(), Optional.empty(), partition);
        } catch (GraphError.GraphErrorException e) {
            return new DocumentBuilder(e.getError());
        }
    }


    /**
     * translate vertex to document
     *
     * @param context
     * @param node
     * @return
     */
    private DocumentBuilder translate(DataTransformerContext<LogicalGraphModel> context, LogicalNode node) {
        try {
            ObjectNode element = mapper.createObjectNode();
            Entity entity = indexProvider.getEntity(node.label())
                    .orElseThrow(() -> new GraphError.GraphErrorException(new GraphError("Logical Graph Transformation Error", "No matching node found with label " + node.label())));
            //translate entity
            translateEntity(mapper, indexProvider, accessor, context, node, element, entity);

            return new DocumentBuilder(element, node.getId(), entity.getType(), Optional.empty());
        } catch (GraphError.GraphErrorException e) {
            return new DocumentBuilder(e.getError());
        }
    }

    static ObjectNode populate(ObjectMapper mapper, Ontology.Accessor accessor, IndexProvider indexProvider, DataTransformerContext<LogicalGraphModel> context, ObjectNode element, Map.Entry<String, Object> m) {
        String pType = accessor.property$(m.getKey()).getpType();
        String type = accessor.property$(m.getKey()).getType();

        Object result = parseValue(accessor.property$(m.getKey()).getType(), m.getValue(), Utils.sdf);

        //case of primitive type
        if (String.class.isAssignableFrom(result.getClass())) {
            return element.put(pType, result.toString());
        }

        //check property is of type struct  -
        if (accessor.entity(type).isPresent() && indexProvider.getEntity(type).isPresent()) {
            //if struct manage as entity
            if (Collection.class.isAssignableFrom(result.getClass())) {
                AtomicInteger index = new AtomicInteger();
                ArrayNode nodes = element.putArray(pType);
                ((Collection) result).forEach(e -> {
                    try {
                        ObjectNode nested = mapper.createObjectNode();
                        LogicalNode node = mapper.readValue(mapper.writeValueAsString(e), LogicalNode.class);
                        //set metadata
                        node.setId(String.format("%s.%d", pType, index.incrementAndGet()));
                        node.setLabel(type);
                        //transform into document
                        translateEntity(mapper, indexProvider, accessor, context, node, nested, indexProvider.getEntity(type).get());
                        nodes.add(nested);
                    } catch (IOException ex) {
                        nodes.addPOJO(e);
                    }
                });
                return element;
            } else {
                return element.putPOJO(pType, result);
            }
        }
        //all primitive non string types
        return element.put(pType, result.toString());
    }

    static void translateEntity(ObjectMapper mapper, IndexProvider indexProvider, Ontology.Accessor accessor, DataTransformerContext<LogicalGraphModel> context, LogicalNode node, ObjectNode element, Entity entity) {
        element.put(EngineIndexProviderMappingFactory.ID, node.getId());
        element.put(EngineIndexProviderMappingFactory.TYPE, entity.getType());

        //populate metadata
        populateMetadataFields(mapper, indexProvider, accessor, context, node, element);

        //populate fields
        populateFields(mapper, indexProvider, accessor, context, node, entity, element);
    }

    /**
     * metadata edge populator
     *
     * @param context
     * @param edge
     * @param element
     */
    public static void populateMetadataFields(ObjectMapper mapper, IndexProvider indexProvider, Ontology.Accessor accessor, DataTransformerContext<LogicalGraphModel> context, LogicalEdge edge, ObjectNode element) {
        edge.metadata().entrySet()
                .stream()
                .filter(m -> accessor.relation$(edge.getLabel()).containsMetadata(m.getKey()))
                .forEach(m -> populate(mapper, accessor, indexProvider, context, element, m));
    }

    /**
     * metadata vertex populator
     *
     * @param context
     * @param node
     * @param element
     */
    public static void populateMetadataFields(ObjectMapper mapper, IndexProvider indexProvider, Ontology.Accessor accessor, DataTransformerContext<LogicalGraphModel> context, LogicalNode node, ObjectNode element) {
        node.metadata().entrySet()
                .stream()
                .filter(m -> accessor.entity$(node.getLabel()).containsMetadata(m.getKey()))
                .forEach(m -> populate(mapper, accessor, indexProvider, context, element, m));
    }


    /**
     * fields vertex populator
     *
     * @param context
     * @param node
     * @param entity
     * @param element
     */
    public static void populateFields(ObjectMapper mapper, IndexProvider indexProvider, Ontology.Accessor accessor, DataTransformerContext<LogicalGraphModel> context, LogicalNode node, Entity entity, ObjectNode element) {
        //todo check the structure of the index
        switch (entity.getMapping()) {
            case EngineIndexProviderMappingFactory.CHILD:
            case Utils.INDEX:
                //populate properties
                node.fields().entrySet()
                        .stream()
                        .filter(m -> accessor.entity$(node.getLabel()).containsProperty(m.getKey()))
                        .forEach(m -> populate(mapper, accessor, indexProvider, context, element, m));
                break;
            // todo manage nested index fields
            default:
        }
    }


    /**
     * populate fields including redundant fields
     *
     * @param edge
     * @param relation
     * @param element
     */
    private ObjectNode populateFields(DataTransformerContext<LogicalGraphModel> context, LogicalEdge edge, Relation relation, String direction, ObjectNode element) {
        //populate redundant fields A
        switch (direction) {
            case "out":
                element.put(EngineIndexProviderMappingFactory.ENTITY_A, populateSide(EngineIndexProviderMappingFactory.ENTITY_A, context, edge.getSource(), relation));
                //populate redundant fields B
                element.put(EngineIndexProviderMappingFactory.ENTITY_B, populateSide(EngineIndexProviderMappingFactory.ENTITY_B, context, edge.getTarget(), relation));
                break;
            case "in":
                element.put(EngineIndexProviderMappingFactory.ENTITY_B, populateSide(EngineIndexProviderMappingFactory.ENTITY_A, context, edge.getSource(), relation));
                //populate redundant fields B
                element.put(EngineIndexProviderMappingFactory.ENTITY_A, populateSide(EngineIndexProviderMappingFactory.ENTITY_B, context, edge.getTarget(), relation));
                break;
        }

        //populate direct fields
        switch (relation.getMapping()) {
            case Utils.INDEX:
                //populate properties
                edge.fields().entrySet()
                        .stream()
                        .filter(m -> accessor.relation$(edge.getLabel()).containsProperty(m.getKey()))
                        .forEach(m -> populate(mapper,accessor,indexProvider,context, element, m));
                break;
            // todo manage nested index fields
            default:
        }

        return element;

    }

    /**
     * populate edge redundant side - as a json object
     *
     * @param side
     * @param context
     * @param sideId
     * @param relation
     * @return
     */
    private ObjectNode populateSide(String side, DataTransformerContext<LogicalGraphModel> context, String sideId, Relation relation) {
        ObjectNode entitySide = mapper.createObjectNode();
        Optional<LogicalNode> source = nodeById(context, sideId);
        if (!source.isPresent()) {
            throw new GraphError.GraphErrorException(new GraphError("Logical Graph Transformation Error", "No matching node found with sideId " + sideId));
        }

        //get type (label) of the side node
        Entity entity = indexProvider.getEntity(source.get().label())
                .orElseThrow(() -> new GraphError.GraphErrorException(new GraphError("Logical Graph Transformation Error", "No matching node found with label " + source.get().label())));

        //put classifiers
        entitySide.put(EngineIndexProviderMappingFactory.ID, source.get().getId());
        entitySide.put(EngineIndexProviderMappingFactory.TYPE, entity.getType());

        List<Redundant> redundant = relation.getRedundant(side);
        redundant.forEach(r -> populateRedundantField(r, source.get(), entitySide));
        return entitySide;
    }

    private void populateRedundantField(Redundant redundant, LogicalNode logicalNode, ObjectNode map) {
        Optional<Object> prop = logicalNode.getPropertyValue(redundant.getRedundantName());
        prop.ifPresent(o -> map.put(redundant.getRedundantName(),
                parseValue(redundant.getType(), o.toString(), Utils.sdf).toString()));
    }

    private Optional<LogicalNode> nodeById(DataTransformerContext<LogicalGraphModel> context, String id) {
        return context.getContainer().getNodes().stream().filter(n -> n.getId().equals(id)).findAny();
    }


}
