package org.opensearch.graph.unipop.controller.discrete.appender;


import org.opensearch.graph.unipop.controller.common.context.VertexControllerContext;
import org.opensearch.graph.unipop.controller.promise.appender.SearchQueryAppenderBase;
import org.opensearch.graph.unipop.controller.search.AggregationBuilder;
import org.opensearch.graph.unipop.controller.search.QueryBuilder;
import org.opensearch.graph.unipop.controller.utils.traversal.TraversalQueryTranslator;
import org.opensearch.graph.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import org.opensearch.graph.unipop.schemaProviders.GraphEdgeSchema;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Set;

import static org.opensearch.graph.unipop.controller.common.appender.EdgeUtils.getLabel;

public class DualEdgeDirectionSearchAppender extends SearchQueryAppenderBase<VertexControllerContext> {
    //region SearchQueryAppenderBase Implementation
    @Override
    protected boolean append(QueryBuilder queryBuilder, AggregationBuilder aggregationBuilder, VertexControllerContext context) {
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
        if (!edgeSchema.getDirectionSchema().isPresent()) {
            return false;
        }

        if (context.getDirection().equals(Direction.BOTH)) {
            return false;
        }

        Traversal directionConstraint = __.start();
        switch (context.getDirection()) {
            case OUT:
                    directionConstraint = __.has(edgeSchema.getDirectionSchema().get().getField(), edgeSchema.getDirectionSchema().get().getOutValue());
                break;
            case IN:
                    directionConstraint = __.has(edgeSchema.getDirectionSchema().get().getField(), edgeSchema.getDirectionSchema().get().getInValue());
                break;
        }

        QueryBuilder builder = queryBuilder.seekRoot().query().bool().filter().bool().must();
        AggregationBuilder aggBuilder = aggregationBuilder.seekRoot();
        TraversalQueryTranslator traversalQueryTranslator =
                new TraversalQueryTranslator(builder, aggBuilder, false);
        traversalQueryTranslator.visit(directionConstraint);
        return true;
    }
    //endregion
}
