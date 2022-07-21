package org.opensearch.graph.unipop.controller.search;


import javaslang.collection.Stream;
import org.opensearch.action.search.SearchRequestBuilder;
import org.opensearch.client.Client;
import org.opensearch.search.aggregations.bucket.composite.CompositeAggregationBuilder;
import org.opensearch.search.aggregations.bucket.terms.TermsAggregationBuilder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lior.perry on 26/03/2017.
 */
public class SearchBuilder {
    //region Constructor
    public SearchBuilder() {
        this.includeSourceFields = new HashSet<>();
        this.excludeSourceFields = new HashSet<>();
        this.indices = new HashSet<>();
        this.types = new HashSet<>();
        this.routing = new HashSet<>();

        this.queryBuilder = new QueryBuilder();
        this.aggregationBuilder = new AggregationBuilder();
    }
    //endregion

    //region Properties
    public QueryBuilder getQueryBuilder() {
        return this.queryBuilder;
    }

    public void setQueryBuilder(QueryBuilder value) {
        this.queryBuilder = value;
    }

    public AggregationBuilder getAggregationBuilder() {
        return this.aggregationBuilder;
    }

    public void setAggregationBuilder(AggregationBuilder aggregationBuilder) {
        this.aggregationBuilder = aggregationBuilder;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long value) {
        this.size = value;
    }

    public long getLimit() {
        return this.limit;
    }

    public void setLimit(long value) {
        this.limit = value;
    }

    public int getScrollSize() {
        return this.scrollSize;
    }

    public void setScrollSize(int value) {
        this.scrollSize = value;
    }

    public int getScrollTime() {
        return this.scrollTime;
    }

    public void setScrollTime(int value) {
        this.scrollTime = value;
    }

    public Collection<String> getIncludeSourceFields() {
        return this.includeSourceFields;
    }

    public Collection<String> getExcludeSourceFields() {
        return this.excludeSourceFields;
    }

    public Collection<String> getIndices() {
        return this.indices;
    }

    public Collection<String> getTypes() {
        return this.types;
    }

    public Collection<String> getRouting() {
        return this.routing;
    }
    //endregion

    //region API
    public SearchRequestBuilder build(
            Client client,
            boolean includeAggregations) {
        String[] indices = getIndices().stream().toArray(String[]::new);
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch();
        searchRequestBuilder.setQuery(queryBuilder.getQuery());
        searchRequestBuilder.setSize((int) getLimit());
        searchRequestBuilder.setIndices(indices);

        if (!routing.isEmpty()) {
            searchRequestBuilder.setRouting(Stream.ofAll(this.routing).toJavaArray(String.class));
        }


        if (getIncludeSourceFields().size() == 0) {
            searchRequestBuilder.setFetchSource(false);
        } else {
            searchRequestBuilder.setFetchSource(
                    Stream.ofAll(getIncludeSourceFields()).toJavaArray(String.class),
                    Stream.ofAll(getExcludeSourceFields()).toJavaArray(String.class));
        }

        if (includeAggregations) {
            aggregationBuilder.getAggregations()
                    .forEach(agg -> {
                        enforceSize(agg, (int) getSize());
                        searchRequestBuilder.addAggregation(agg);
                    });
        }

        return searchRequestBuilder;
    }

    /**
     * enforce size for different potential aggs types
     *
     * @param agg
     * @param size
     */
    private void enforceSize(org.opensearch.search.aggregations.AggregationBuilder agg, int size) {
        if (size > 0) {
            if (TermsAggregationBuilder.class.isAssignableFrom(agg.getClass())) {
                ((TermsAggregationBuilder) agg).size(size);
                return;
            }
            if (CompositeAggregationBuilder.class.isAssignableFrom(agg.getClass())) {
                ((TermsAggregationBuilder) agg).size(size);
                return;
            }
        }
        //todo add any size supported aggregation here
    }
    //endregion

    //region Fields
    private Collection<String> types;
    private Collection<String> includeSourceFields;
    private Collection<String> excludeSourceFields;
    private Collection<String> indices;
    private Collection<String> routing;
    private Set<String> labels;

    private QueryBuilder queryBuilder;
    private AggregationBuilder aggregationBuilder;

    private long size;
    private long limit;
    private int scrollSize;
    private int scrollTime;
    //endregion
}
