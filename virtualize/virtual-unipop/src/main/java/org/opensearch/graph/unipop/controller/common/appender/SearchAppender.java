package org.opensearch.graph.unipop.controller.common.appender;


import org.opensearch.graph.unipop.controller.search.SearchBuilder;

/**
 * Created by lior.perry on 27/03/2017.
 */
public interface SearchAppender<TContext> {
    boolean append(SearchBuilder searchBuilder, TContext context);
}
