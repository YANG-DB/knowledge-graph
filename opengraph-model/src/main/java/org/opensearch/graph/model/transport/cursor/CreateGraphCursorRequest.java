package org.opensearch.graph.model.transport.cursor;




import org.opensearch.graph.model.transport.CreatePageRequest;

/**
 * Created by roman.margolis on 11/03/2018.
 */
public class CreateGraphCursorRequest extends CreateCursorRequest {
    public static final String CursorType = "graph";

    //region Constructors
    public CreateGraphCursorRequest() {
        super(CursorType);
    }

    public CreateGraphCursorRequest(CreatePageRequest createPageRequest) {
        super(CursorType, createPageRequest);
    }

    public CreateGraphCursorRequest(Include include, CreatePageRequest createPageRequest) {
        super(CursorType, include, createPageRequest);
    }

    public CreateGraphCursorRequest(String cursorType, Include include, CreatePageRequest createPageRequest) {
        super(cursorType,include,createPageRequest);
    }

    public CreateGraphCursorRequest(String cursorType, Include include, CreatePageRequest createPageRequest,GraphFormat format) {
        super(cursorType,include,createPageRequest);
        this.format = format;
    }

    public GraphFormat getFormat() {
        return format;
    }

    //endregion
    private GraphFormat format = GraphFormat.JSON;

    public enum GraphFormat {
        JSON,XML
    }
    //endregion
}
