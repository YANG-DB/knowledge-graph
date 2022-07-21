package org.opensearch.graph.model.transport.cursor;


import org.opensearch.graph.model.transport.CreatePageRequest;

public class FindPathTraversalCursorRequest extends CreateCursorRequest {

    public static final String CursorType = "findPaths";
    private int amount;

    public FindPathTraversalCursorRequest() {
    }

    //region Constructors
    public FindPathTraversalCursorRequest(int amount) {
        super(CursorType);
        this.amount = amount;
    }

    public FindPathTraversalCursorRequest(int amount, CreatePageRequest createPageRequest) {
        super(CursorType, createPageRequest);
        this.amount = amount;
    }

    public FindPathTraversalCursorRequest(int amount, Include include, CreatePageRequest createPageRequest) {
        super(CursorType, include, createPageRequest);
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
}
