package org.opensearch.graph.unipop.controller.utils;



import org.opensearch.graph.unipop.controller.search.SearchBuilder;
import org.opensearch.graph.unipop.controller.common.appender.SearchAppender;

public class SearchAppenderUtil {
    public static <TContext, SContext> SearchAppender<SContext> wrap(SearchAppender<TContext> searchAppender) {
        return (SearchBuilder searchBuilder, SContext context) -> searchAppender.append(searchBuilder, (TContext)context);
    }
}
