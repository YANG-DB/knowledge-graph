package org.opensearch.graph.unipop.controller.common.appender;


import org.opensearch.graph.unipop.controller.common.context.ElementControllerContext;
import org.opensearch.graph.unipop.controller.promise.appender.SearchQueryAppenderBase;
import org.opensearch.graph.unipop.controller.search.AggregationBuilder;
import org.opensearch.graph.unipop.controller.search.QueryBuilder;

/**
 * Created by lior.perry on 27/03/2017.
 */
@Deprecated
public class ElementGlobalTypeSearchAppender extends SearchQueryAppenderBase<ElementControllerContext> {
    //region SearchQueryAppenderBase Implementation
    @Override
    public boolean append(QueryBuilder queryBuilder, AggregationBuilder aggregationBuilder, ElementControllerContext context) {
       /* OptionalComp<TraversalConstraint> constraint = context.getConstraint();
        if (constraint.isPresent()) {
            TraversalValuesByKeyProvider traversalValuesByKeyProvider = new TraversalValuesByKeyProvider();
            Set<String> labels = traversalValuesByKeyProvider.getValueByKey(context.getConstraint().get().getTraversal(), T.label.getAccessor());

            // If there are labels in the constraint, this appender is not relevant, exit.
            if (!labels.isEmpty())
                return false;
        }
        // If there is no Constraint
        if (context.getElementType() == ElementType.vertex) {
            Iterable<String> vertexLabels = Stream.ofAll(context.getSchemaProvider().getVertexLabels())
                    .map(label -> context.getSchemaProvider().getVertexSchemas(label).get().getTyped())
                    .toJavaList();
            queryBuilder.seekRoot().query().filtered().filter().bool().must().terms(this.getClass().getSimpleName(),"_type", vertexLabels);
        }
        else if (context.getElementType() == ElementType.edge) {
            Iterable<String> edgeLabels = Stream.ofAll(context.getSchemaProvider().getEdgeLabels())
                    .map(label -> context.getSchemaProvider().getEdgeSchema(label).get().getTyped())
                    .toJavaList();
            queryBuilder.seekRoot().query().filtered().filter().bool().must().terms(this.getClass().getSimpleName(),"_type", edgeLabels);
        }*/

        return true;
    }
    //endregion
}
