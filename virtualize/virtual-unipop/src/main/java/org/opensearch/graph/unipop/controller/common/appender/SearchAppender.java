package org.opensearch.graph.unipop.controller.common.appender;


import org.opensearch.graph.unipop.controller.search.SearchBuilder;

public interface SearchAppender<TContext> {
    boolean append(SearchBuilder searchBuilder, TContext context);
}
