package org.opensearch.graph.unipop.controller.utils;


import org.opensearch.graph.unipop.controller.search.SearchBuilder;
import org.opensearch.graph.unipop.controller.common.appender.SearchAppender;

/**
 * Created by Roman on 15/05/2017.
 */
public class SearchAppenderUtil {
    public static <TContext, SContext> SearchAppender<SContext> wrap(SearchAppender<TContext> searchAppender) {
        return (SearchBuilder searchBuilder, SContext context) -> searchAppender.append(searchBuilder, (TContext)context);
    }
}
