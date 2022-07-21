package org.opensearch.graph.dispatcher.urlSupplier;




public class DefaultAppUrlSupplier implements AppUrlSupplier {
    //region Constructors
    public DefaultAppUrlSupplier(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    //endregion

    //region AppUrlSupplier Implementation
    @Override
    public String resourceUrl(String queryId) {
        return new ResourceUrlSupplier(this.baseUrl).queryId(queryId).get();
    }

    @Override
    public String resourceUrl(String queryId, String cursorId) {
        return new ResourceUrlSupplier(this.baseUrl).queryId(queryId).cursorId(cursorId).get();
    }

    @Override
    public String resourceUrl(String queryId, String cursorId, String pageId) {
        return new ResourceUrlSupplier(this.baseUrl).queryId(queryId).cursorId(cursorId).pageId(pageId).get();
    }

    @Override
    public String resourceUrl(String queryId, String cursorId, String pageId, String format) {
        return new ResourceUrlSupplier(this.baseUrl).queryId(queryId).cursorId(cursorId).pageId(pageId).format(format).get();
    }

    @Override
    public String baseUrl() {
        return baseUrl;
    }

    @Override
    public String queryStoreUrl() {
        return new ResourceStoreUrlSupplier(this.baseUrl, ResourceStoreUrlSupplier.Store.Query).get();
    }

    @Override
    public String cursorStoreUrl(String queryId) {
        return new ResourceStoreUrlSupplier(this.baseUrl, ResourceStoreUrlSupplier.Store.Cursor).queryId(queryId).get();
    }

    @Override
    public String pageStoreUrl(String queryId, String cursorId) {
        return new ResourceStoreUrlSupplier(this.baseUrl, ResourceStoreUrlSupplier.Store.Page).queryId(queryId).cursorId(cursorId).get();
    }

    @Override
    public String catalogStoreUrl() {
        return this.baseUrl + "/catalog/ontology";
    }
    //endregion

    //region Fields
    protected String baseUrl;
    //endregion
}
