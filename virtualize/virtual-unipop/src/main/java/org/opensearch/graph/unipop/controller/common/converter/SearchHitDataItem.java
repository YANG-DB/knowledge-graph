package org.opensearch.graph.unipop.controller.common.converter;


import org.opensearch.search.SearchHit;

import java.util.Map;

/**
 * Created by roman.margolis on 14/03/2018.
 */
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
