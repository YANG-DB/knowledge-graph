package org.opensearch.graph.unipop.controller.common.appender;


import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.unipop.controller.common.context.VertexControllerContext;
import org.opensearch.graph.unipop.controller.search.SearchBuilder;
import org.opensearch.graph.unipop.controller.utils.ElementUtil;
import org.opensearch.graph.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import org.opensearch.graph.unipop.schemaProviders.GraphEdgeSchema;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.opensearch.graph.unipop.controller.common.appender.EdgeUtils.getLabel;

/**
 * Created by roman.margolis on 18/10/2017.
 */
public class EdgeRoutingSearchAppender implements SearchAppender<VertexControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, VertexControllerContext context) {
        Set<String> labels = context.getConstraint().isPresent() ?
                new TraversalValuesByKeyProvider().getValueByKey(context.getConstraint().get().getTraversal(), T.label.getAccessor()) :
                Stream.ofAll(context.getSchemaProvider().getEdgeLabels()).toJavaSet();

        //currently assuming a single edge label
        String edgeLabel = Stream.ofAll(labels).get(0);

        //currently assuming a single vertex label in bulk
        String contextVertexLabel = getLabel(context,"?");


        Iterable<GraphEdgeSchema> edgeSchemas = context.getSchemaProvider().getEdgeSchemas(contextVertexLabel, context.getDirection(), edgeLabel);
        if (Stream.ofAll(edgeSchemas).isEmpty()) {
            return false;
        }

        //currently assuming only one relevant schema
        GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);
        GraphEdgeSchema.End endSchema = edgeSchema.getEndA().get();

        if (endSchema.getRouting().isPresent()) {
            boolean isRoutingFieldFullyAvailable =
                    Stream.ofAll(context.getBulkVertices())
                            .map(vertex -> ElementUtil.<String>value(vertex,
                                    translateRoutingPropertyName(endSchema.getRouting().get().getRoutingProperty().getName())))
                            .filter(value -> !value.isPresent())
                            .size() == 0;

            Set<String> routingValues = Collections.emptySet();
            if (isRoutingFieldFullyAvailable) {
                routingValues =
                        Stream.ofAll(context.getBulkVertices())
                                .map(vertex -> ElementUtil.<String>value(vertex,
                                        translateRoutingPropertyName(endSchema.getRouting().get().getRoutingProperty().getName())))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .toJavaSet();
            }

            searchBuilder.getRouting().addAll(routingValues);
            return routingValues.size() > 0;
        }

        return false;
    }
    //endregion

    //region Private Methods
    private String translateRoutingPropertyName(String name) {
        return name.equals(GlobalConstants._ID) ? T.id.getAccessor() : name;
    }
    //endregion
}
