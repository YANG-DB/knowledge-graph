package org.opensearch.graph.dispatcher.urlSupplier;




import java.util.function.Supplier;

/**
 * Created by lior.perry on 08/03/2017.
 */
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
