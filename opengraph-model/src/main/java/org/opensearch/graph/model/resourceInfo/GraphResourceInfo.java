package org.opensearch.graph.model.resourceInfo;






/**
 * Created by lior.perry on 09/03/2017.
 */
public class GraphResourceInfo extends ResourceInfoBase {
    //region Constructors
    public GraphResourceInfo() {
    }

    public GraphResourceInfo(String resourceUrl, String internalUrl, String healthUrl, String queryStoreUrl, String searchStoreUrl, String catalogStoreUrl) {
        super(resourceUrl, null);
        this.healthUrl = healthUrl;
        this.internal = internalUrl;
        this.queryStoreUrl = queryStoreUrl;
        this.searchStoreUrl = searchStoreUrl;
        this.catalogStoreUrl = catalogStoreUrl;
    }
    //endregion

    //region Properties
    public String getHealthUrl() {
        return this.healthUrl;
    }

    public String getInternal() {
        return internal;
    }

    public String getQueryStoreUrl() {
        return this.queryStoreUrl;
    }

    public String getSearchStoreUrl() {
        return this.searchStoreUrl;
    }

    public String getCatalogStoreUrl() {
        return this.catalogStoreUrl;
    }

    public void setHealthUrl(String healthUrl) {
        this.healthUrl = healthUrl;
    }

    public void setQueryStoreUrl(String queryStoreUrl) {
        this.queryStoreUrl = queryStoreUrl;
    }

    public void setSearchStoreUrl(String searchStoreUrl) {
        this.searchStoreUrl = searchStoreUrl;
    }

    public void setCatalogStoreUrl(String catalogStoreUrl) {
        this.catalogStoreUrl = catalogStoreUrl;
    }

    //endregion

    //region Fields
    private String healthUrl;
    private String internal;
    private String queryStoreUrl;
    private String searchStoreUrl;
    private String catalogStoreUrl;
    //endregion
}
