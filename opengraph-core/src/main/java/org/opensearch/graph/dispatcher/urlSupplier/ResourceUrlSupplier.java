package org.opensearch.graph.dispatcher.urlSupplier;







import java.util.Optional;

public class ResourceUrlSupplier extends UrlSupplierBase {
    //region Constructors
    public ResourceUrlSupplier(String baseUrl) {
        super(baseUrl);
        this.queryId = Optional.empty();
        this.cursorId = Optional.empty();
        this.pageId = Optional.empty();
        this.format = Optional.empty();
    }
    //endregion

    //region Public Methods
    public ResourceUrlSupplier queryId(String queryId) {
        ResourceUrlSupplier clone = cloneImpl();
        clone.queryId = Optional.of(queryId);
        return clone;
    }

    public ResourceUrlSupplier cursorId(String cursorId) {
        ResourceUrlSupplier clone = cloneImpl();
        clone.cursorId = Optional.of(cursorId);
        return clone;
    }

    public ResourceUrlSupplier pageId(String pageId) {
        ResourceUrlSupplier clone = cloneImpl();
        clone.pageId = Optional.of(pageId);
        return clone;
    }

    public ResourceUrlSupplier format(String format) {
        ResourceUrlSupplier clone = cloneImpl();
        clone.format = Optional.of(format);
        return clone;
    }
    //endregion

    //region UrlSupplierBase Implementation
    @Override
    public String get() {
        if (!this.queryId.isPresent()) {
            return null;
        }

        if (!this.cursorId.isPresent()) {
            return this.baseUrl + "/query/" + this.queryId.get();
        }

        if (!this.pageId.isPresent()) {
            return this.baseUrl + "/query/" + this.queryId.get() + "/cursor/" + this.cursorId.get();
        }

        if (!this.format.isPresent()) {
            return this.baseUrl + "/query/" + this.queryId.get() + "/cursor/" + this.cursorId.get() + "/page/" + this.pageId.get();
        }

        return this.baseUrl + "/query/" + this.queryId.get() + "/cursor/" + this.cursorId.get() + "/page/" + this.pageId.get() +"/format/"+this.format.get();
    }
    //endregion

    //region Protected Methods
    protected ResourceUrlSupplier cloneImpl() {
        ResourceUrlSupplier clone = new ResourceUrlSupplier(this.baseUrl);
        clone.queryId = this.queryId;
        clone.cursorId = this.cursorId;
        clone.pageId = this.pageId;
        clone.format = this.format;
        return clone;
    }
    //endregion

    //region Fields
    protected Optional<String> queryId;
    protected Optional<String> cursorId;
    protected Optional<String> pageId;
    protected Optional<String> format;
    //endregion
}
