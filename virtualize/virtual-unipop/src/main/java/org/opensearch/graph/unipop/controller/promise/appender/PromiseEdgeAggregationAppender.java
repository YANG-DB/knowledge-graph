package org.opensearch.graph.unipop.controller.promise.appender;


import org.opensearch.graph.unipop.controller.common.appender.SearchAppender;
import org.opensearch.graph.unipop.controller.common.context.ElementControllerContext;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.unipop.controller.search.SearchBuilder;

public class PromiseEdgeAggregationAppender implements SearchAppender<ElementControllerContext> {
    @Override
    public boolean append(SearchBuilder searchBuilder, ElementControllerContext context) {
        searchBuilder.getAggregationBuilder().seekRoot()
                     .terms(GlobalConstants.EdgeSchema.SOURCE)
                        .field(GlobalConstants.EdgeSchema.SOURCE_ID)
                        .size(1000)
                        .shardSize(1000)
                        .executionHint("map")
                     .terms(GlobalConstants.EdgeSchema.DEST)
                        .field(GlobalConstants.EdgeSchema.DEST_ID)
                        .size(1000)
                        .shardSize(1000)
                        .executionHint("map");

        return true;
    }
}
