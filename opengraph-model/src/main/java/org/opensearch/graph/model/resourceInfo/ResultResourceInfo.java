package org.opensearch.graph.model.resourceInfo;



public class ResultResourceInfo<T> extends ResourceInfoBase {

    public ResultResourceInfo(String resourceUrl, String resourceId, T result) {
        super(resourceUrl, resourceId);
        this.result = result;
    }

    public ResultResourceInfo(String resourceUrl, String resourceId, FuseError error) {
        super(resourceUrl, resourceId);
        this.error = error;
    }

    public T getResult() {
        return result;
    }

    public FuseError getError() {
        return error;
    }

    public boolean isError() {
        return error!=null;
    }

    private FuseError error;
    private T result;

}
