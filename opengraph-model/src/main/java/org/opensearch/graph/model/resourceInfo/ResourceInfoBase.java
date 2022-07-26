package org.opensearch.graph.model.resourceInfo;






/**
 * Created by lior.perry on 09/03/2017.
 */
public abstract class ResourceInfoBase {
    //region Constructor
    public ResourceInfoBase() {}

    public ResourceInfoBase(String resourceUrl,String resourceId) {
        this.resourceUrl = resourceUrl;
        this.resourceId = resourceId;
    }
    //endregion

    //region properties
    public String getResourceUrl() {
        return this.resourceUrl;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    //endregion

    //region Fields
    private String resourceUrl;
    private String resourceId;
    //endregion

}
