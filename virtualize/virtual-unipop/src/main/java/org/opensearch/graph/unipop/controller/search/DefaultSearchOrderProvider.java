package org.opensearch.graph.unipop.controller.search;

/*-
 * #%L
 * virtual-unipop
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */





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
