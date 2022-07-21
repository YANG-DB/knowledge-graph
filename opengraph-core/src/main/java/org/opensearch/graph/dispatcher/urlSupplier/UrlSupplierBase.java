package org.opensearch.graph.dispatcher.urlSupplier;




import java.util.function.Supplier;

public abstract class UrlSupplierBase implements Supplier<String> {
    //region Constructors
    public UrlSupplierBase(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    //endregion

    //region Fields
    protected String baseUrl;
    //endregion
}
