package org.opensearch.graph.executor.ontology.schema.load;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import org.opensearch.graph.dispatcher.driver.IdGeneratorDriver;
import org.opensearch.graph.dispatcher.ontology.IndexProviderFactory;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.executor.opensearch.EngineIndexProviderMappingFactory;
import org.opensearch.graph.executor.ontology.DataTransformer;
import org.opensearch.graph.executor.ontology.schema.RawSchema;
import org.opensearch.graph.model.Range;
import org.opensearch.graph.model.ontology.EntityType;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.RelationshipType;
import org.opensearch.graph.model.resourceInfo.FuseError;
import org.opensearch.graph.model.schema.Entity;
import org.opensearch.graph.model.schema.IndexProvider;
import org.opensearch.graph.model.schema.Redundant;
import org.opensearch.graph.model.schema.Relation;
import javaslang.Tuple2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.opensearch.client.Client;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

import static org.opensearch.graph.model.GlobalConstants.EdgeSchema.DEST_TYPE;

public class CSVTransformer implements DataTransformer<DataTransformerContext, CSVTransformer.CsvElement> {
    private final Ontology.Accessor accessor;
    private IndexProvider indexProvider;
    private final RawSchema schema;
    private final IdGeneratorDriver<Range> idGenerator;
    private final Client client;
    private final ObjectMapper mapper;

    @Inject
    public CSVTransformer(Config config, OntologyProvider ontology, IndexProviderFactory indexProvider, RawSchema schema, IdGeneratorDriver<Range> idGenerator, Client client) {
        String assembly = config.getString("assembly");
        this.accessor = new Ontology.Accessor(ontology.get(assembly).orElseThrow(
                () -> new FuseError.FuseErrorException(new FuseError("No Ontology present for assembly", "No Ontology present for assembly" + assembly))));

        //if no index provider found with assembly name - generate default one accoring to ontology and simple Static Index Partitioning strategy
        this.indexProvider = indexProvider.get(assembly).orElseGet(() -> IndexProvider.Builder.generate(accessor.get()));

        this.schema = schema;
        this.idGenerator = idGenerator;
        this.client = client;
        this.mapper = new ObjectMapper();

    }

    @Override
    public DataTransformerContext transform(CSVTransformer.CsvElement data, GraphDataLoader.Directive directive) {
        DataTransformerContext context = new DataTransformerContext(mapper);
        try (CSVParser csvRecords = new CSVParser(data.content(), CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withIgnoreHeaderCase()
                .withTrim())) {

            List<CSVRecord> dataRecords = csvRecords.getRecords();
            if (accessor.entity(data.label()).isPresent()) {
                EntityType entityType = accessor.entity$(data.label());
                dataRecords.forEach(r -> context.withEntity(translate(context, entityType, r.toMap())));
            } else if (accessor.relation(data.label()).isPresent()) {
                RelationshipType relType = accessor.relation$(data.label());
                //store both sides
                dataRecords.forEach(r -> context.withRelation(translate(context, relType, r.toMap(), "in")));
                dataRecords.forEach(r -> context.withRelation(translate(context, relType, r.toMap(), "out")));
            }
        } catch (IOException e) {
            throw new FuseError.FuseErrorException("Error while building graph Element from csv row ", e);
        }
        return context;
    }

    private DocumentBuilder translate(DataTransformerContext context, EntityType entityType, Map<String, String> node) {
        try {
            ObjectNode element = mapper.createObjectNode();
            Entity entity = indexProvider.getEntity(entityType.geteType())
                    .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("CSV Transformation Error", "No matching node found with label " + entityType.geteType())));
            //put id (take according to ontology id field mapping or generate UUID of none found)
            StringJoiner joiner = new StringJoiner(".");
            entityType.getIdField().forEach(field -> joiner.add(node.getOrDefault(field, UUID.randomUUID().toString())));
            //put classifiers
            element.put(entityType.idFieldName(), joiner.toString());
            element.put(Utils.TYPE, entity.getType());

            //populate fields
            populateMetadataFields(context, node, entity, element);
            populatePropertyFields(context, node, entity, element);
            return new DocumentBuilder(element, joiner.toString(), entity.getType(), Optional.empty());
        } catch (FuseError.FuseErrorException e) {
            return new DocumentBuilder(e.getError());
        }
    }

    /**
     * transform single relation-row into documents (including sideA / sideB proxies)
     *
     * @param context
     * @param relType
     * @param node
     * @return
     */
    private DocumentBuilder translate(DataTransformerContext context, RelationshipType relType, Map<String, String> node, String direction) {
        try {
            ObjectNode element = mapper.createObjectNode();
            Relation relation = indexProvider.getRelation(relType.getrType())
                    .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("CSV Transformation Error", "No matching node found with label " + relType.getrType())));
            //put id (take according to ontology id field mapping or generate UUID of none found)
            StringJoiner joiner = new StringJoiner(".");
            relType.getIdField().forEach(field -> joiner.add(node.getOrDefault(field, UUID.randomUUID().toString())));
            //put classifiers
            String id = String.format("%s.%s", joiner.toString(), direction);
            //put classifiers
            element.put(relType.idFieldName(), id);
            element.put(EngineIndexProviderMappingFactory.TYPE, relation.getType());
            element.put(EngineIndexProviderMappingFactory.DIRECTION, direction);

            //populate fields
            populateMetadataFields(context, node, relation, element);

            populatePropertyFields(context, node, relation, element, direction);

            //partition field in case of none static partitioning index
            Optional<Tuple2<String, String>> partition = Optional.empty();

            //in case of a partition field - set in the document builder
            String field = relation.getProps().getPartitionField();
            if (field != null)
                partition = Optional.of(new Tuple2<>(field, DataLoaderUtils.parseValue(accessor.property$(field).getType(), node.get(field), Utils.sdf).toString()));

            return new DocumentBuilder(element, id, relation.getType(), Optional.empty(), partition);
        } catch (FuseError.FuseErrorException e) {
            return new DocumentBuilder(e.getError());
        }
    }


    /**
     * Relation metadata populator
     *
     * @param context
     * @param element
     */
    private void populateMetadataFields(DataTransformerContext context, Map<String, String> node, Relation relation, ObjectNode element) {
        node.entrySet()
                .stream()
                .filter(m -> accessor.$relation$(relation.getType()).containsMetadata(m.getKey()))
                .filter(m -> DataLoaderUtils.validateValue(accessor.property$(m.getKey()).getType(), m.getValue(), Utils.sdf))
                .forEach(m -> element.put(accessor.property$(m.getKey()).getpType(),
                        DataLoaderUtils.parseValue(accessor.property$(m.getKey()).getType(), m.getValue(), Utils.sdf).toString()));
    }


    /**
     * Relation PropertyFields populator
     *
     * @param context
     * @param element
     * @param direction
     */
    private void populatePropertyFields(DataTransformerContext context, Map<String, String> node, Relation relation, ObjectNode element, String direction) {
        node.entrySet()
                .stream()
                .filter(m -> accessor.$relation$(relation.getType()).containsProperty(m.getKey()))
                .filter(m -> DataLoaderUtils.validateValue(accessor.property$(m.getKey()).getType(), m.getValue(), Utils.sdf))
                .forEach(m -> element.put(accessor.property$(m.getKey()).getpType(),
                        DataLoaderUtils.parseValue(accessor.property$(m.getKey()).getType(), m.getValue(), Utils.sdf).toString()));

        RelationshipType relationshipType = accessor.$relation$(relation.getType());
        //populate each pair
        switch (direction) {
            case "out":
                //for each pair do:
                relationshipType.getePairs().stream().filter(pair -> pair.geteTypeB().equalsIgnoreCase(node.get(DEST_TYPE)))
                        .forEach(pair -> {
                            element.put(EngineIndexProviderMappingFactory.ENTITY_A, populateSide(EngineIndexProviderMappingFactory.ENTITY_A, context, node.get(pair.getSideAIdField()), pair.geteTypeA(), relation, node));
                            element.put(EngineIndexProviderMappingFactory.ENTITY_B, populateSide(EngineIndexProviderMappingFactory.ENTITY_B, context, node.get(pair.getSideBIdField()), pair.geteTypeB(), relation, node));
                        });
                break;
            case "in":
                //for each pair do:
                relationshipType.getePairs().stream().filter(pair -> pair.geteTypeB().equalsIgnoreCase(node.get(DEST_TYPE)))
                        .forEach(pair -> {
                            element.put(EngineIndexProviderMappingFactory.ENTITY_B, populateSide(EngineIndexProviderMappingFactory.ENTITY_A, context, node.get(pair.getSideAIdField()), pair.geteTypeA(), relation, node));
                            element.put(EngineIndexProviderMappingFactory.ENTITY_A, populateSide(EngineIndexProviderMappingFactory.ENTITY_B, context, node.get(pair.getSideBIdField()), pair.geteTypeB(), relation, node));
                        });
                break;
        }
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
    private ObjectNode populateSide(String side, DataTransformerContext context, String sideId, String sideType, Relation relation, Map<String, String> node) {
        ObjectNode entitySide = mapper.createObjectNode();

        //get type (label) of the side node
        Entity entity = indexProvider.getEntity(sideType)
                .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("Logical Graph Transformation Error", "No matching node found with label " + sideType)));

        //put classifiers
        entitySide.put(EngineIndexProviderMappingFactory.ID, sideId);
        entitySide.put(Utils.TYPE, entity.getType());

        List<Redundant> redundant = relation.getRedundant(side);
        redundant.forEach(r -> populateRedundantField(r, node, side, entitySide));
        return entitySide;
    }

    private void populateRedundantField(Redundant redundant, Map<String, String> node, String sideName, ObjectNode map) {
        String key = String.format("%s.%s", sideName, redundant.getRedundantName());
        if (node.containsKey(key))
            map.put(redundant.getRedundantName(), DataLoaderUtils.parseValue(redundant.getType(), node.get(key), Utils.sdf).toString());
    }


    /**
     * Entity metadata populator
     *
     * @param context
     * @param element
     */
    private void populateMetadataFields(DataTransformerContext context, Map<String, String> node, Entity entity, ObjectNode element) {
        node.entrySet()
                .stream()
                .filter(m -> accessor.$entity$(entity.getType()).containsMetadata(m.getKey()))
                .filter(m -> DataLoaderUtils.validateValue(accessor.property$(m.getKey()).getType(), m.getValue(), Utils.sdf))
                .forEach(m -> element.put(accessor.property$(m.getKey()).getpType(),
                        DataLoaderUtils.parseValue(accessor.property$(m.getKey()).getType(), m.getValue(), Utils.sdf).toString()));
    }


    /**
     * Entity PropertyFields populator
     *
     * @param context
     * @param element
     */
    private void populatePropertyFields(DataTransformerContext context, Map<String, String> node, Entity entity, ObjectNode element) {
        node.entrySet()
                .stream()
                .filter(m -> accessor.$entity$(entity.getType()).containsProperty(m.getKey()))
                .filter(m -> DataLoaderUtils.validateValue(accessor.property$(m.getKey()).getType(), m.getValue(), Utils.sdf))
                .forEach(m -> element.put(accessor.property$(m.getKey()).getpType(),
                        DataLoaderUtils.parseValue(accessor.property$(m.getKey()).getType(), m.getValue(), Utils.sdf).toString()));
    }


    /**
     * Csv Graph element Container
     */
    public interface CsvElement {
        String label();

        /**
         * todo - calculate the type according to the ontology
         *
         * @return
         */
        String type();

        Reader content();
    }


}
