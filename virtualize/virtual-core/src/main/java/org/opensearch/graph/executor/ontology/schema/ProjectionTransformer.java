package org.opensearch.graph.executor.ontology.schema;

/*-
 * #%L
 * virtual-core
 * %%
 * Copyright (C) 2016 - 2021 The YangDb Graph Database Project
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
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.opensearch.graph.executor.ontology.DataTransformer;
import org.opensearch.graph.executor.ontology.schema.load.DataLoaderUtils;
import org.opensearch.graph.executor.ontology.schema.load.DataTransformerContext;
import org.opensearch.graph.executor.ontology.schema.load.DocumentBuilder;
import org.opensearch.graph.executor.ontology.schema.load.GraphDataLoader;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.projection.ProjectionAssignment;
import org.opensearch.graph.model.projection.ProjectionEdge;
import org.opensearch.graph.model.projection.ProjectionNode;
import org.opensearch.graph.model.results.Assignment;
import org.opensearch.graph.model.results.AssignmentsQueryResult;
import org.opensearch.graph.model.results.Entity;
import org.opensearch.graph.model.results.Relationship;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.opensearch.graph.model.GlobalConstants.EdgeSchema.DEST_ID;
import static org.opensearch.graph.model.GlobalConstants.EdgeSchema.DEST_TYPE;
import static org.opensearch.graph.model.GlobalConstants.ID;
import static org.opensearch.graph.model.GlobalConstants.ProjectionConfigs.TAG;
import static org.opensearch.graph.model.GlobalConstants.TYPE;

/**
 * transforms a single assignment (row) into a projection document (containing list of nodes and within each node a list of first hop edges)
 */
public class ProjectionTransformer implements DataTransformer<DataTransformerContext<List<ProjectionAssignment>>, AssignmentsQueryResult<Entity, Relationship>> {
    private static ObjectMapper mapper = new ObjectMapper();
    private final Ontology.Accessor accessor;
    private static HashFunction hashFunction = Hashing.murmur3_128();


    public ProjectionTransformer(Ontology.Accessor accessor) {
        this.accessor = accessor;
    }

    @Override
    public DataTransformerContext<List<ProjectionAssignment>> transform(AssignmentsQueryResult<Entity, Relationship> data, GraphDataLoader.Directive directive) {
        DataTransformerContext<List<ProjectionAssignment>> context = new DataTransformerContext<>(mapper);
        context.withContainer(data.getAssignments().stream().map(a -> buildAssignment(data.getQueryId(), data.getCursorId(), data.getTimestamp(), a)).collect(Collectors.toList()));
        translate(context);
        return context;
    }

    private void translate(DataTransformerContext<List<ProjectionAssignment>> context) {
        List<ProjectionAssignment> container = context.getContainer();
        //translate assignments to root level entities - all relations are embedded inside the entities as nested document
        context.withEntities(container.stream().map(this::translate).collect(Collectors.toList()));
    }

    private DocumentBuilder translate(ProjectionAssignment row) {
        ObjectNode rootEntity = mapper.createObjectNode();
        //root level metadata
        rootEntity.put(TYPE, row.getType());
        rootEntity.put(GlobalConstants.ProjectionConfigs.QUERY_ID, row.getQueryId());
        rootEntity.put(GlobalConstants.ProjectionConfigs.CURSOR_ID, row.getCursorId());
        rootEntity.put(GlobalConstants.ProjectionConfigs.EXECUTION_TIME, row.getTimestamp());
        row.getNodes().forEach(node -> translate(rootEntity, node));

        return new DocumentBuilder(rootEntity, String.valueOf(row.getId()), row.getType(), Optional.empty());
    }

    private void translate(ObjectNode root, ProjectionNode node) {
        if (root.get(node.label()) == null) {
            //create the specific node array for the internal nested document
            root.put(node.label(), mapper.createArrayNode());
        }
        com.fasterxml.jackson.databind.node.ArrayNode arrayNode = (ArrayNode) root.get(node.label());

        ObjectNode element = mapper.createObjectNode();
        //node level metadata
        element.put(ID, node.getId());
        element.put(TYPE, node.getLabel());
        element.put(TAG, node.tag());
        //node properties
        node.getProperties().getProperties().forEach((key, value) -> populateField(key, value, element));
        //node inner edges which are nested documents
        node.getProjectionEdge().forEach(edge -> translate(element, edge));
        arrayNode.add(element);
    }

    private void translate(ObjectNode rootNode, ProjectionEdge edge) {
        if (rootNode.get(edge.label()) == null) {
            //create the specific node array for the internal nested document
            rootNode.put(edge.label(), mapper.createArrayNode());
        }
        com.fasterxml.jackson.databind.node.ArrayNode arrayEdge = (ArrayNode) rootNode.get(edge.label());

        ObjectNode element = mapper.createObjectNode();
        //edge level metadata
        element.put(ID, edge.getId());
        element.put(TYPE, edge.getLabel());
        element.put(TAG, edge.tag());
        element.put(DEST_TYPE, edge.getTargetLabel());
        element.put(DEST_ID, edge.getTargetId());
        //edge properties
        edge.getProperties().getProperties().forEach((key, value) -> populateField(key, value, element));
        arrayEdge.add(element);
    }

    private void populateField(String key, Object value, ObjectNode element) {
        String pType = accessor.property$(key).getpType();
        Object result = DataLoaderUtils.parseValue(accessor.property$(key).getType(), value, Utils.sdf);
        //all primitive non string types
        element.put(pType, result.toString());
    }

    private ProjectionAssignment buildAssignment(String queryId, String cursorId, long timestamp, Assignment<Entity, Relationship> assignment) {
        HashCode hashCode = hashFunction.hashBytes((queryId + cursorId + assignment.toString()).getBytes());
        ProjectionAssignment projection = new ProjectionAssignment(hashCode.asLong(), queryId, cursorId, timestamp);
        projection.withAll(assignment.getEntities().stream().map(a -> translate(a, assignment)).collect(Collectors.toList()));
        return projection;
    }

    private ProjectionNode translate(Entity entity, Assignment<Entity, Relationship> assignment) {
        ProjectionNode node = new ProjectionNode(entity.id(), entity.geteType());
        node.withTag(entity.tag());
        node.withProperties(entity.getProperties());

        assignment.getRelationBySource(entity.geteID())
                .forEach(rel -> node.withEdge(translate(rel, assignment)));
        return node;
    }

    private ProjectionEdge translate(Relationship relationship, Assignment<Entity, Relationship> assignment) {
        String targetLabel = assignment.getEntityById(relationship.geteID2()).get().label();
        return new ProjectionEdge(relationship.id(), relationship.tag(), relationship.label(),
                targetLabel, relationship.geteID2(), relationship.isDirectional());
    }
}
