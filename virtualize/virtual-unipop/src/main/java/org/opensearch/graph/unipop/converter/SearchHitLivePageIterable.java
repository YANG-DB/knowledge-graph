package org.opensearch.graph.unipop.converter;


import org.opensearch.graph.unipop.controller.search.SearchOrderProvider;
import org.opensearch.action.search.SearchRequestBuilder;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.search.SearchType;
import org.opensearch.client.Client;
import org.opensearch.search.SearchHit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class SearchHitLivePageIterable implements Iterable<SearchHit> {
    //region Constructor
    public SearchHitLivePageIterable(
            Client client,
            SearchRequestBuilder searchRequestBuilder,
            SearchOrderProvider orderProvider,
            long limit,
            int scrollSize) {
        this.searchRequestBuilder = searchRequestBuilder;
        this.orderProvider = orderProvider;
        this.limit = limit;
        this.scrollSize = scrollSize;
        this.client = client;
    }
    //endregion

    //region Iterable Implementation
    @Override
    public Iterator<SearchHit> iterator() {
        return new LivePageIterator(this);
    }
    //endregion

    //region Properties
    protected SearchRequestBuilder getSearchRequestBuilder() {
        return this.searchRequestBuilder;
    }

    public SearchOrderProvider getOrderProvider() {
        return orderProvider;
    }

    protected Client getClient() {
        return this.client;
    }

    protected long getLimit() {
        return this.limit;
    }

    public int getScrollSize() {
        return this.scrollSize;
    }
    //endregion

    //region Fields
    private SearchRequestBuilder searchRequestBuilder;
    private SearchOrderProvider orderProvider;
    private long limit;
    private Client client;

    private int scrollSize;
    //endregion


    //region Iterator
    private class LivePageIterator implements Iterator<SearchHit> {
        //region Constructor
        private LivePageIterator(SearchHitLivePageIterable iterable) {
            this.iterable = iterable;
            this.searchHits = new ArrayList<>(iterable.getScrollSize());
        }
        //endregion

        //region Iterator Implementation
        @Override
        public boolean hasNext() {
            if (this.searchHits.size() > 0) {
                return true;
            }

            Scroll();

            return this.searchHits.size() > 0;

        }

        @Override
        public SearchHit next() {
            if (this.searchHits.size() > 0) {
                SearchHit searchHit = this.searchHits.get(0);
                this.searchHits.remove(0);
                return searchHit;
            }

            Scroll();

            if (this.searchHits.size() > 0) {
                SearchHit searchHit = this.searchHits.get(0);
                this.searchHits.remove(0);

                return searchHit;
            }

            throw new NoSuchElementException();
        }
        //endregion

        //region Private Methods
        private void Scroll() {
            if (counter >= this.iterable.getLimit()) {
                return;
            }

            //next page
            SearchResponse response = getSearchResponse();

            if(response.getHits().getHits().length > 0) {
                this.pageStartId = response.getHits().getHits()[response.getHits().getHits().length - 1].getId();
            }

            for (SearchHit hit : response.getHits().getHits()) {
                if (counter < this.iterable.getLimit()) {
                    this.searchHits.add(hit);
                    counter++;
                }
            }
        }

        private SearchResponse getSearchResponse() {
            SearchOrderProvider.Sort sort = getOrderProvider().getSort(this.iterable.getSearchRequestBuilder());
            SearchType searchType = getOrderProvider().getSearchType(this.iterable.getSearchRequestBuilder());

            return sort != SearchOrderProvider.EMPTY ?
                    this.iterable.getSearchRequestBuilder()
                            .addSort(sort.getSortField(), sort.getSortOrder())
                            .setSearchType(searchType)
                            .setSize(Math.min(iterable.getScrollSize(),
                                    (int) Math.min((long) Integer.MAX_VALUE, iterable.getLimit())))
                            .setFrom((int) counter)
                            .execute()
                            .actionGet() :
                    this.iterable.getSearchRequestBuilder()
                            .setSearchType(searchType)
                            .setSize(Math.min(iterable.getScrollSize(),
                                    (int) Math.min((long) Integer.MAX_VALUE, iterable.getLimit())))
                            .setFrom((int) counter)
                            .execute()
                            .actionGet();
        }
        //endregion

        //region Fields
        private SearchHitLivePageIterable iterable;
        private ArrayList<SearchHit> searchHits;
        private String pageStartId;
        private long counter;
        //endregion
    }
    //endregion
}
