package org.opensearch.graph.unipop.controller.common.appender;


import org.opensearch.graph.unipop.controller.common.context.ElementControllerContext;
import org.opensearch.graph.unipop.controller.search.SearchBuilder;

public class NormalizeRoutingSearchAppender implements SearchAppender<ElementControllerContext> {
    //region Constructors
    public NormalizeRoutingSearchAppender(int maxNumValues) {
        this.maxNumValues = maxNumValues;
    }
    //endregion

    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, ElementControllerContext context) {
        if (searchBuilder.getRouting().size() > maxNumValues) {
            searchBuilder.getRouting().clear();
            return true;
        }

        return false;
    }
    //endregion

    //region Fields
    private int maxNumValues;
    //endregion
}
