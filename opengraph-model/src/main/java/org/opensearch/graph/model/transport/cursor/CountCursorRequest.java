package org.opensearch.graph.model.transport.cursor;



import org.opensearch.graph.model.transport.CreatePageRequest;

public class CountCursorRequest extends CreateCursorRequest {

    public static final String CursorType = "count";
    //region Constructors
    public CountCursorRequest() {
        super(CursorType);
    }

    public CountCursorRequest(CreatePageRequest createPageRequest) {
        super(CursorType, createPageRequest);
    }

    public CountCursorRequest(Include include, CreatePageRequest createPageRequest) {
        super(CursorType, include, createPageRequest);
    }
}
