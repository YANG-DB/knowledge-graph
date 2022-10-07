package org.opensearch.graph.unipop.controller.discrete.converter;

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





import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.unipop.controller.common.context.VertexControllerContext;
import org.opensearch.graph.unipop.controller.common.converter.DataItem;
import org.opensearch.graph.unipop.controller.common.converter.ElementConverter;
import org.opensearch.graph.unipop.controller.utils.idProvider.EdgeIdProvider;
import org.opensearch.graph.unipop.controller.utils.idProvider.HashEdgeIdProvider;
import org.opensearch.graph.unipop.controller.utils.idProvider.SimpleEdgeIdProvider;
import org.opensearch.graph.unipop.controller.utils.map.MapHelper;
import org.opensearch.graph.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import org.opensearch.graph.unipop.schema.providers.GraphEdgeSchema;
import org.opensearch.graph.unipop.schema.providers.GraphElementPropertySchema;
import org.opensearch.graph.unipop.schema.providers.GraphRedundantPropertySchema;
import org.opensearch.graph.unipop.structure.discrete.DiscreteEdge;
import org.opensearch.graph.unipop.structure.discrete.DiscreteVertex;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.process.Profiler;

import java.util.*;

import static org.opensearch.graph.unipop.controller.common.appender.EdgeUtils.getLabel;

public class DiscreteEdgeConverter<E extends Element> implements ElementConverter<DataItem, E> {
    //region Constructors
    public DiscreteEdgeConverter(VertexControllerContext context, Profiler profiler) {
        this.context = context;
        try {
            this.edgeIdProvider = new HashEdgeIdProvider(context.getConstraint());
        } catch (Exception e) {
            e.printStackTrace();
            this.edgeIdProvider = new SimpleEdgeIdProvider();
        }

        //currently assuming a single vertex label in bulk
        this.contextVertexLabel = getLabel(context, "?");

        Set<String> labels = this.context.getConstraint().isPresent() ?
                new TraversalValuesByKeyProvider().getValueByKey(this.context.getConstraint().get().getTraversal(), T.label.getAccessor()) :
                Stream.ofAll(context.getSchemaProvider().getEdgeLabels()).toJavaSet();

        //currently assuming a single edge label
        this.contextEdgeLabel = Stream.ofAll(labels).get(0);

        this.profiler = profiler;
        context.getStepDescriptor().getDescription()
                .ifPresent(v -> this.profiler.get().setCount(v, 0));
    }
    //endregion

    //region ElementConverter Implementation
    @Override
    public Iterable<E> convert(DataItem dataItem) {
        Map<String, Object> dataItemProperties = dataItem.properties();

        Iterator<GraphEdgeSchema> edgeSchemas = context.getSchemaProvider().getEdgeSchemas(this.contextVertexLabel, context.getDirection(), this.contextEdgeLabel).iterator();
        if (!edgeSchemas.hasNext()) {
            return null;
        }

        //currently assuming only one relevant edge schema
        GraphEdgeSchema edgeSchema = edgeSchemas.next();

        Vertex outV = null;
        Vertex inV = null;

        Collection<E> edges = new ArrayList<>();

        if (edgeSchema.getDirection().equals(Direction.OUT)) {
            GraphEdgeSchema.End outEndSchema = edgeSchema.getEndA().get();
            GraphEdgeSchema.End inEndSchema = edgeSchema.getEndB().get();


            Map<String, Object> inVertexProperties = createVertexProperties(inEndSchema, dataItemProperties);
            Map<String, Object> edgeProperties = createEdgeProperties(edgeSchema, dataItemProperties);

            Iterable<Object> outIds = getIdFieldValues(dataItem, outEndSchema.getIdFields());
            Iterable<Object> inIds = getIdFieldValues(dataItem, inEndSchema.getIdFields());

            for (Object outId : outIds) {
                for (Object inId : inIds) {
                    outV = context.getVertex(outId);

                    // could happen in multi value relation fields where the document contains additional values
                    // than those available in the query context
                    if (outV == null) {
                        continue;
                    }

                    inV = new DiscreteVertex(inId, inEndSchema.getLabel().get(), context.getGraph(), inVertexProperties);

                    edges.add((E) new DiscreteEdge(
                            this.edgeIdProvider.get(edgeSchema.getLabel().getName(), outV, inV, edgeProperties),
                            edgeSchema.getLabel().getName(),
                            outV,
                            inV,
                            inV,
                            context.getGraph(),
                            edgeProperties));
                }
            }

        } else {
            GraphEdgeSchema.End outEndSchema = edgeSchema.getEndB().get();
            GraphEdgeSchema.End inEndSchema = edgeSchema.getEndA().get();

            Map<String, Object> outVertexProperties = createVertexProperties(outEndSchema, dataItemProperties);
            Map<String, Object> edgeProperties = createEdgeProperties(edgeSchema, dataItemProperties);

            Iterable<Object> outIds = getIdFieldValues(dataItem, outEndSchema.getIdFields());
            Iterable<Object> inIds = getIdFieldValues(dataItem, inEndSchema.getIdFields());

            for (Object outId : outIds) {
                for (Object inId : inIds) {
                    inV = context.getVertex(inId);

                    // could happen in multi value relation fields where the document contains additional values
                    // than those available in the query context
                    if (inV == null) {
                        continue;
                    }

                    outV = new DiscreteVertex(outId, outEndSchema.getLabel().get(), context.getGraph(), outVertexProperties);

                    edges.add((E) new DiscreteEdge(
                            this.edgeIdProvider.get(edgeSchema.getLabel().getName(), outV, inV, edgeProperties),
                            edgeSchema.getLabel().getName(),
                            outV,
                            inV,
                            outV,
                            context.getGraph(),
                            edgeProperties));
                }
            }
        }
        String stepName = context.getStepDescriptor().getDescription().orElse(contextEdgeLabel);
        profiler.get().incrementCount(stepName, 1);

        return edges;
    }

    //endregion

    //region Private Methods
    private Iterable<Object> getIdFieldValues(DataItem dataItem, Iterable<String> idFields) {
        List<Object> idFieldValues = Collections.emptyList();
        boolean isfirst = true;
        boolean isSecond = false;
        for (String idField : idFields) {
            if (isfirst) {
                idFieldValues = getIdFieldValues(dataItem, idField);
                isfirst = false;
                isSecond = true;
            } else if (isSecond) {
                idFieldValues = new ArrayList<>(idFieldValues);
                idFieldValues.addAll(getIdFieldValues(dataItem, idField));
                isSecond = false;
            } else {
                idFieldValues.addAll(getIdFieldValues(dataItem, idField));
            }
        }

        return idFieldValues;
    }

    private List<Object> getIdFieldValues(DataItem dataItem, String idField) {
        if (idField.equals(GlobalConstants._ID)) {
            return Collections.singletonList(dataItem.id());
        } else {
            return MapHelper.values(dataItem.properties(), idField);
        }
    }

    private Map<String, Object> createVertexProperties(GraphEdgeSchema.End endSchema, Map<String, Object> properties) {
        Optional<GraphRedundantPropertySchema> partitionField = endSchema.getIndexPartitions().isPresent() ?
                Optional.of(new GraphRedundantPropertySchema.Impl(
                        endSchema.getIndexPartitions().get().getPartitionField().get(),
                        endSchema.getIndexPartitions().get().getPartitionField().get(),
                        "string")) :
                Optional.empty();

        Optional<GraphRedundantPropertySchema> routingField = endSchema.getRouting().isPresent() ?
                Optional.of(new GraphRedundantPropertySchema.Impl(
                        endSchema.getRouting().get().getRoutingProperty().getName(),
                        endSchema.getRouting().get().getRoutingProperty().getName(),
                        "string")) :
                Optional.empty();

        Map<String, Object> vertexProperties = new HashMap<>();
        Iterable<GraphRedundantPropertySchema> redundantPropertySchemas = endSchema.getRedundantProperties();

        if (partitionField.isPresent()) {
            Object propertyValue = properties.get(partitionField.get().getPropertyRedundantName());
            if (propertyValue != null) {
                vertexProperties.put(partitionField.get().getName(), propertyValue);
            }
        }

        if (routingField.isPresent()) {
            Object propertyValue = properties.get(routingField.get().getPropertyRedundantName());
            if (propertyValue != null) {
                vertexProperties.put(routingField.get().getName(), propertyValue);
            }
        }

        for (GraphRedundantPropertySchema redundantPropertySchema : redundantPropertySchemas) {
            Object propertyValue = properties.get(redundantPropertySchema.getPropertyRedundantName());
            if (propertyValue != null) {
                vertexProperties.put(redundantPropertySchema.getName(), propertyValue);
            }
        }

        return vertexProperties;
    }

    private Map<String, Object> createEdgeProperties(GraphEdgeSchema schema, Map<String, Object> properties) {
        Map<String, Object> edgeProperties = new HashMap<>();
        for (GraphElementPropertySchema property : schema.getProperties()) {
            if (properties.containsKey(property.getName())) {
                edgeProperties.put(property.getName(), properties.get(property.getName()));
            }
        }
        return edgeProperties;
    }
    //endregion

    //endregion
    @Override
    public Profiler getProfiler() {
        return this.profiler;
    }

    @Override
    public void setProfiler(Profiler profiler) {
        this.profiler = profiler;
    }

    //region Fields
    private Profiler profiler = Profiler.Noop.instance;
    private VertexControllerContext context;
    private EdgeIdProvider<String> edgeIdProvider;

    private String contextVertexLabel;
    private String contextEdgeLabel;
    //endregion
}
