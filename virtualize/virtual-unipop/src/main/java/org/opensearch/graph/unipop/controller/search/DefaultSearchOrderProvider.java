package org.opensearch.graph.unipop.controller.search;



import org.opensearch.graph.unipop.controller.common.context.CompositeControllerContext;
import org.opensearch.action.search.SearchRequestBuilder;
import org.opensearch.action.search.SearchType;
import org.opensearch.search.sort.FieldSortBuilder;
import org.opensearch.search.sort.SortOrder;

public class DefaultSearchOrderProvider implements SearchOrderProviderFactory {
    @Override
    public SearchOrderProvider build(CompositeControllerContext context) {
        return new SearchOrderProvider() {
            @Override
            public Sort getSort(SearchRequestBuilder builder) {
                return new Sort(FieldSortBuilder.DOC_FIELD_NAME,SortOrder.ASC);
            }

            @Override
            public SearchType getSearchType(SearchRequestBuilder builder) {
                return SearchType.DEFAULT;
            }
        };
    }
}
