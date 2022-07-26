package org.opensearch.graph.dispatcher.resource;







import java.util.Date;

public class PageResource<T> {

    //region Constructors
    public PageResource(String pageId, int requestedSize) {
        this.pageId = pageId;
        this.timeCreated = new Date(System.currentTimeMillis());
        this.requestedSize = requestedSize;
        this.isAvailable = false;
    }

    public PageResource(String pageId, T data, int requestedSize, long executionTime) {
        this.pageId = pageId;
        this.timeCreated = new Date(System.currentTimeMillis());
        this.executionTime = executionTime;
        this.data = data;
        this.requestedSize = requestedSize;
        this.isAvailable = false;
    }
    //endregion

    //region Public Methods
    public PageResource<T> withResults(T data) {
        PageResource<T> clone = this.cloneImpl();
        clone.data = data;
        return clone;
    }

    public PageResource<T> withActualSize(int actualSize) {
        PageResource<T> clone = this.cloneImpl();
        clone.actualSize = actualSize;
        return clone;
    }

    public PageResource<T> available() {
        PageResource<T> clone = this.cloneImpl();
        clone.isAvailable = true;
        return clone;
    }
    //endregion

    //region properties
    public String getPageId() {
        return this.pageId;
    }

    public Date getTimeCreated() {
        return this.timeCreated;
    }

    public T getData() {
        return this.data;
    }

    public int getRequestedSize() {
        return this.requestedSize;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public int getActualSize() {
        return this.actualSize;
    }

    public boolean isAvailable() {
        return this.isAvailable;
    }

    public PageState getState() {
        return isAvailable?PageState.ACTIVE:PageState.PREPARING;
    }
    //endregion

    //region Private Methods
    private PageResource<T> cloneImpl() {
        PageResource<T> clone = new PageResource<T>(this.pageId, this.data, this.requestedSize,this.executionTime);
        clone.timeCreated = this.timeCreated;
        clone.actualSize = this.actualSize;
        clone.isAvailable = this.isAvailable;
        return clone;
    }
    //endregion

    //region Fields
    private String pageId;
    private Date timeCreated;
    private T data;
    private long executionTime;
    private int requestedSize;
    private int actualSize;
    private boolean isAvailable;
    //endregion
}
