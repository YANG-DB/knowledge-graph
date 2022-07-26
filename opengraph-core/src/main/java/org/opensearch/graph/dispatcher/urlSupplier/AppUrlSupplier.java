package org.opensearch.graph.dispatcher.urlSupplier;







public interface AppUrlSupplier {
    String resourceUrl(String queryId);
    String resourceUrl(String queryId, String cursorId);
    String resourceUrl(String queryId, String cursorId, String pageId);
    String resourceUrl(String queryId, String cursorId, String pageId, String format);

    String baseUrl();
    String queryStoreUrl();
    String cursorStoreUrl(String queryId);
    String pageStoreUrl(String queryId, String cursorId);

    String catalogStoreUrl();
}
