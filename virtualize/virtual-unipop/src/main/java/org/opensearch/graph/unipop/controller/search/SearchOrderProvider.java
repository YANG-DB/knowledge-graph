package org.opensearch.graph.unipop.controller.search;





import org.opensearch.action.search.SearchRequestBuilder;
import org.opensearch.action.search.SearchType;
import org.opensearch.search.sort.SortOrder;

import java.util.Objects;

public interface SearchOrderProvider {
    Sort EMPTY = new Sort(null,null);

    Sort getSort(SearchRequestBuilder builder);

    SearchType getSearchType(SearchRequestBuilder builder);


    static SearchOrderProvider of(Sort sort , SearchType search) {
        return new SearchOrderProvider() {

            @Override
            public Sort getSort(SearchRequestBuilder builder) {
                return sort;
            }

            @Override
            public SearchType getSearchType(SearchRequestBuilder builder) {
                return search;
            }
        };
    }


    final class Sort {
        private final String sortField;
        private final SortOrder sortOrder;

        public Sort(String sortField, SortOrder sortOrder) {
            this.sortField = sortField;
            this.sortOrder = sortOrder;
        }

        public String getSortField() {
            return sortField;
        }

        public SortOrder getSortOrder() {
            return sortOrder;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Sort sort = (Sort) o;
            return Objects.equals(sortField, sort.sortField) &&
                    sortOrder == sort.sortOrder;
        }

        @Override
        public int hashCode() {

            return Objects.hash(sortField, sortOrder);
        }
    }
}
