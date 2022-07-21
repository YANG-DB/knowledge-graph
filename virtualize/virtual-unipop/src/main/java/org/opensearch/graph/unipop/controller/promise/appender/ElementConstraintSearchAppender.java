package org.opensearch.graph.unipop.controller.promise.appender;


import org.opensearch.graph.unipop.controller.common.context.ElementControllerContext;
import org.opensearch.graph.unipop.controller.search.AggregationBuilder;
import org.opensearch.graph.unipop.controller.search.QueryBuilder;
import org.opensearch.graph.unipop.controller.utils.traversal.TraversalQueryTranslator;

public class ElementConstraintSearchAppender extends SearchQueryAppenderBase<ElementControllerContext> {
    //region SearchQueryAppenderBase Implementation
    @Override
    protected boolean append(QueryBuilder queryBuilder, AggregationBuilder aggregationBuilder, ElementControllerContext context) {
        if (!context.getConstraint().isPresent()) {
            return false;
        }
        new TraversalQueryTranslator(queryBuilder.seekRoot().query().bool().filter().bool().must(),aggregationBuilder.seekRoot() , false)
                .visit(context.getConstraint().get().getTraversal());

        return true;
    }
    //endregion
}
