package org.opensearch.graph.unipop.controller.common.converter;

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





import org.opensearch.search.SearchHit;

import java.util.Map;

public class SearchHitDataItem implements DataItem {
    //region Constructors
    public SearchHitDataItem(SearchHit searchHit) {
        this.searchHit = searchHit;
    }
    //endregion

    //region DataItem Implementation
    @Override
    public Object id() {
        return this.searchHit.getId();
    }

    @Override
    public Map<String, Object> properties() {
        if (this.properties == null) {
            //        ##Es 5 optimization for searchHit deprecated Logger
            //        this.properties = SearchHitUtils.convertToMap(this.searchHit);
            this.properties = this.searchHit.getSourceAsMap();
        }

        return this.properties;
    }
    //endregion

    //region Fields
    private SearchHit searchHit;
    private Map<String, Object> properties;
    //endregion
}
