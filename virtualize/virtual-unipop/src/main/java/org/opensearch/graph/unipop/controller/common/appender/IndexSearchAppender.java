package org.opensearch.graph.unipop.controller.common.appender;


import org.opensearch.graph.unipop.controller.common.context.ElementControllerContext;
import org.opensearch.graph.unipop.controller.search.SearchBuilder;
import org.opensearch.graph.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import org.opensearch.graph.unipop.promise.TraversalConstraint;
import org.opensearch.graph.unipop.schemaProviders.GraphEdgeSchema;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;
import org.opensearch.graph.unipop.schemaProviders.GraphVertexSchema;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.IndexPartitions;
import org.opensearch.graph.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.*;


// This appender will add getIndices getTo the search builder based on the elements IndexPartitions only.
public class IndexSearchAppender implements SearchAppender<ElementControllerContext> {

    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, ElementControllerContext context) {
        GraphElementSchemaProvider schemaProvider = context.getSchemaProvider();
        Optional<TraversalConstraint> constraint = context.getConstraint();
        if (!constraint.isPresent()) {
            manageSpecialCase(context, schemaProvider, searchBuilder);
        } else {
            Traversal traversal = constraint.get().getTraversal();
            TraversalValuesByKeyProvider traversalValuesByKeyProvider = new TraversalValuesByKeyProvider();
            Set<String> labels = traversalValuesByKeyProvider.getValueByKey(traversal, T.label.getAccessor());
            if (!labels.isEmpty()) {
                labels.stream().forEach(label -> {
                    if (context.getElementType() == ElementType.edge) {
                        Iterable<GraphEdgeSchema> edgeSchemas = schemaProvider.getEdgeSchemas(label);
                        searchBuilder.getIndices().addAll(getEdgeSchemasIndices(edgeSchemas));
                    } else if (context.getElementType() == ElementType.vertex) {
                        Iterable<GraphVertexSchema> vertexSchemas = schemaProvider.getVertexSchemas(label);
                        if (!Stream.ofAll(vertexSchemas).isEmpty()) {
                            // currently only supports a single vertex schema
                            searchBuilder.getIndices().addAll(getVertexSchemasIndices(Stream.ofAll(vertexSchemas).get(0)));
                        }
                    }
                });
                return true;
            } else // No specific label - append all index getPartitions filtered by the type of the element (vertex or edge)
            {
                manageSpecialCase(context, schemaProvider, searchBuilder);
            }
        }

        return true;
    }

    private void manageSpecialCase(ElementControllerContext context, GraphElementSchemaProvider schemaProvider, SearchBuilder searchBuilder) {
        if (context.getElementType() == ElementType.vertex) {
            Iterable<String> vertexTypes = schemaProvider.getVertexLabels();
            vertexTypes.forEach(vertexType -> {
                Iterable<GraphVertexSchema> vertexSchemas = schemaProvider.getVertexSchemas(vertexType);
                if (!Stream.ofAll(vertexSchemas).isEmpty()) {
                    // currently only supports a single vertex schema
                    searchBuilder.getIndices().addAll(getVertexSchemasIndices(Stream.ofAll(vertexSchemas).get(0)));
                }
            });
        } else if (context.getElementType() == ElementType.edge) {
            Iterable<String> edgeTypes = schemaProvider.getEdgeLabels();
            edgeTypes.forEach(edgeType -> {
                Iterable<GraphEdgeSchema> edgeSchemas = schemaProvider.getEdgeSchemas(edgeType);
                    searchBuilder.getIndices().addAll(getEdgeSchemasIndices(edgeSchemas));
            });
        }

    }

    //endregion

    //region Private Methods

    private Collection<String> getEdgeSchemasIndices(Iterable<GraphEdgeSchema> edgeSchemas) {
        return Stream.ofAll(edgeSchemas)
                .filter(edgeSchema -> edgeSchema.getIndexPartitions().isPresent())
                .flatMap(edgeSchema -> edgeSchema.getIndexPartitions().get().getPartitions())
                .flatMap(IndexPartitions.Partition::getIndices)
                .toJavaSet();
    }

    private Collection<String> getVertexSchemasIndices(GraphVertexSchema vertexSchema) {
        if (!vertexSchema.getIndexPartitions().isPresent()) {
            return Collections.emptyList();
        }

        return Stream.ofAll(vertexSchema.getIndexPartitions().get().getPartitions())
                .flatMap(IndexPartitions.Partition::getIndices)
                .toJavaSet();
    }

    //endregion
}
