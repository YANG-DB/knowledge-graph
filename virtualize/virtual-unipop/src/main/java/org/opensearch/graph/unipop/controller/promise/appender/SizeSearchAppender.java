package org.opensearch.graph.unipop.controller.promise.appender;



import org.opensearch.graph.unipop.controller.OpensearchGraphConfiguration;
import org.opensearch.graph.unipop.controller.common.appender.SearchAppender;
import org.opensearch.graph.unipop.controller.common.context.LimitContext;
import org.opensearch.graph.unipop.controller.search.SearchBuilder;

public class SizeSearchAppender implements SearchAppender<LimitContext> {
    //region Constructors
    public SizeSearchAppender(OpensearchGraphConfiguration configuration) {
        this.configuration = configuration;
    }
    //endregion

    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, LimitContext context) {
        searchBuilder.setLimit(context.getLimit() < 0 ?
                configuration.getElasticGraphDefaultSearchSize() :
                Math.min(context.getLimit(), configuration.getElasticGraphMaxSearchSize()));

        searchBuilder.setScrollSize(configuration.getElasticGraphScrollSize());
        searchBuilder.setScrollTime(configuration.getElasticGraphScrollTime());

        return true;
    }
    //endregion

    //region Fields
    private OpensearchGraphConfiguration configuration;
    //endregion
}
