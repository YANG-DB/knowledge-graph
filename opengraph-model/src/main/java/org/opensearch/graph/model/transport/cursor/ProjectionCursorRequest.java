package org.opensearch.graph.model.transport.cursor;


import org.opensearch.graph.model.transport.CreatePageRequest;

public class ProjectionCursorRequest extends CreateCursorRequest {

    public static final String CursorType = "projection";
    //region Constructors
    public ProjectionCursorRequest() {
        super(CursorType);
    }

    public ProjectionCursorRequest(CreatePageRequest createPageRequest) {
        super(CursorType, createPageRequest);
    }

    public ProjectionCursorRequest(Include include, CreatePageRequest createPageRequest) {
        super(CursorType, include, createPageRequest);
    }
}
