package org.opensearch.graph.unipop.controller.common.appender;





import org.opensearch.graph.unipop.controller.common.context.VertexControllerContext;
import org.opensearch.graph.unipop.controller.search.SearchBuilder;
import org.opensearch.graph.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import org.opensearch.graph.unipop.schemaProviders.GraphEdgeSchema;
import org.opensearch.graph.unipop.schemaProviders.GraphElementPropertySchema;
import org.opensearch.graph.unipop.schemaProviders.GraphRedundantPropertySchema;
import org.opensearch.graph.unipop.schemaProviders.GraphVertexSchema;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Optional;
import java.util.Set;

import static org.opensearch.graph.unipop.controller.common.appender.EdgeUtils.getLabel;

public class EdgeSourceRoutingSearchAppender  implements SearchAppender<VertexControllerContext> {
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
        GraphEdgeSchema.End otherEndSchema = edgeSchema.getEndB().get();

        Iterable<GraphVertexSchema> otherVertexSchemas = context.getSchemaProvider().getVertexSchemas(otherEndSchema.getLabel().get());

        if (!Stream.ofAll(otherVertexSchemas).isEmpty()) {
            return false;
        }

        GraphVertexSchema otherVertexSchema = Stream.ofAll(otherVertexSchemas).get(0);
        if (!otherVertexSchema.getRouting().isPresent()) {
            return false;
        }


        GraphElementPropertySchema routingProperty = otherVertexSchema.getRouting().get().getRoutingProperty();
        Optional<GraphRedundantPropertySchema> redundnatRoutingProperty = otherEndSchema.getRedundantProperty(routingProperty);
        if (!redundnatRoutingProperty.isPresent()) {
            return false;
        }

        searchBuilder.getIncludeSourceFields().add(redundnatRoutingProperty.get().getPropertyRedundantName());
        return true;
    }
    //endregion
}
