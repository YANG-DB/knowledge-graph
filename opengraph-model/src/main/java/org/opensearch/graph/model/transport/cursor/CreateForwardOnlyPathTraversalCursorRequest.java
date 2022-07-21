package org.opensearch.graph.model.transport.cursor;


import org.opensearch.graph.model.transport.CreatePageRequest;

public class CreateForwardOnlyPathTraversalCursorRequest extends CreateCursorRequest {

    public static final String CursorType = "forwardPaths";
    //region Constructors
    public CreateForwardOnlyPathTraversalCursorRequest() {
        super(CursorType);
    }

    public CreateForwardOnlyPathTraversalCursorRequest(CreatePageRequest createPageRequest) {
        super(CursorType, createPageRequest);
    }

    public CreateForwardOnlyPathTraversalCursorRequest(Include include, CreatePageRequest createPageRequest) {
        super(CursorType, include, createPageRequest);
    }
}
