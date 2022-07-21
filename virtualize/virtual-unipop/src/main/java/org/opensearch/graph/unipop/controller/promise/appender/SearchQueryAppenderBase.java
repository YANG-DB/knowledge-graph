package org.opensearch.graph.unipop.controller.promise.appender;



import org.opensearch.graph.unipop.controller.common.appender.SearchAppender;
import org.opensearch.graph.unipop.controller.search.AggregationBuilder;
import org.opensearch.graph.unipop.controller.search.QueryBuilder;
import org.opensearch.graph.unipop.controller.search.SearchBuilder;

public abstract class SearchQueryAppenderBase<TContext> implements SearchAppender<TContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, TContext context) {
        return append(searchBuilder.getQueryBuilder(),searchBuilder.getAggregationBuilder() , context);
    }
    //endregion

    //region Abstract Methods
    protected abstract boolean append(QueryBuilder queryBuilder, AggregationBuilder aggregationBuilder, TContext context);
    //endregion
}
