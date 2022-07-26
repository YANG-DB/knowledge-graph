package org.opensearch.graph.unipop.controller.common.appender;





import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.unipop.controller.common.context.VertexControllerContext;
import org.opensearch.graph.unipop.controller.search.SearchBuilder;
import org.opensearch.graph.unipop.controller.utils.ElementUtil;
import org.opensearch.graph.unipop.schemaProviders.GraphVertexSchema;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.opensearch.graph.unipop.controller.common.appender.EdgeUtils.getLabel;

public class FilterRoutingSearchAppender implements SearchAppender<VertexControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, VertexControllerContext context) {
        // currently assuming homogeneous vertex bulk
        String vertexLabel = getLabel(context,"?");

        Iterable<GraphVertexSchema> vertexSchemas = context.getSchemaProvider().getVertexSchemas(vertexLabel);
        if (Stream.ofAll(vertexSchemas).isEmpty()) {
            return false;
        }

        // currently supports a single vertex schema
        GraphVertexSchema vertexSchema = Stream.ofAll(vertexSchemas).get(0);

        if (vertexSchema.getRouting().isPresent()) {
            boolean isRoutingFieldFullyAvailable =
                    Stream.ofAll(context.getBulkVertices())
                            .map(vertex -> ElementUtil.<String>value(vertex,
                                    translateRoutingPropertyName(vertexSchema.getRouting().get().getRoutingProperty().getName())))
                            .filter(value -> !value.isPresent())
                            .size() == 0;

            Set<String> routingValues = Collections.emptySet();
            if (isRoutingFieldFullyAvailable) {
               routingValues =
                        Stream.ofAll(context.getBulkVertices())
                                .map(vertex -> ElementUtil.<String>value(vertex,
                                        translateRoutingPropertyName(vertexSchema.getRouting().get().getRoutingProperty().getName())))
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
