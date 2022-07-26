package org.opensearch.graph.model.transport.cursor;






import org.opensearch.graph.model.transport.CreatePageRequest;

/**
 * Created by roman.margolis on 11/03/2018.
 */
public class CreateGraphQLCursorRequest extends CreateCursorRequest {
    public static final String CursorType = "graphQL";

    //region Constructors
    public CreateGraphQLCursorRequest() {
        super(CursorType);
    }

    public CreateGraphQLCursorRequest(CreatePageRequest createPageRequest) {
        super(CursorType, createPageRequest);
    }

    public CreateGraphQLCursorRequest(Include include, CreatePageRequest createPageRequest) {
        super(CursorType, include, createPageRequest);
    }

    public CreateGraphQLCursorRequest(String cursorType, Include include, CreatePageRequest createPageRequest) {
        super(cursorType,include,createPageRequest);
    }

    public CreateGraphQLCursorRequest(String cursorType, Include include, CreatePageRequest createPageRequest, GraphFormat format) {
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
