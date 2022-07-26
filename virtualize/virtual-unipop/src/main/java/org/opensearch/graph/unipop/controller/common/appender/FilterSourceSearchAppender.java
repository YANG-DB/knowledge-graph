package org.opensearch.graph.unipop.controller.common.appender;





import org.opensearch.graph.unipop.controller.common.context.SelectContext;
import org.opensearch.graph.unipop.controller.search.SearchBuilder;
import org.opensearch.graph.unipop.predicates.SelectP;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;

public class FilterSourceSearchAppender implements SearchAppender<SelectContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, SelectContext context) {
        for(HasContainer selectPHasContainer : context.getSelectPHasContainers()) {
            searchBuilder = appendSelectP(searchBuilder, selectPHasContainer.getKey(), selectPHasContainer.getPredicate());
        }

        return true;
    }
    //endregion

    //region Private Methods
    private SearchBuilder appendSelectP(SearchBuilder searchBuilder, String name, P<?> predicate) {
        if (!(predicate.getBiPredicate() instanceof SelectP)) {
            return searchBuilder;
        }

        SelectP selectP = (SelectP)predicate.getBiPredicate();
        switch (selectP) {
            case raw:
                searchBuilder.getIncludeSourceFields().add(predicate.getValue().toString());
                break;
            case intern:
                searchBuilder.getIncludeSourceFields().add(predicate.getValue().toString());
                break;
        }

        return searchBuilder;
    }
    //endregion
}
