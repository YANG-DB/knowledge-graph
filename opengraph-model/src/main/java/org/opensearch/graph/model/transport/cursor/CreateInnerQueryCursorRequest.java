package org.opensearch.graph.model.transport.cursor;




/**
 * Created by lior perry
 */
public class CreateInnerQueryCursorRequest extends CreateCursorRequest {
    public static final String CursorType = "inner";
    private CreateCursorRequest cursorRequest;

    //region Constructors
    public CreateInnerQueryCursorRequest() {
        super(CursorType);
    }

    public CreateInnerQueryCursorRequest(CreateCursorRequest cursorRequest) {
        this();
        this.cursorRequest = cursorRequest;
    }
    //endregion

    public CreateCursorRequest getCursorRequest() {
        return cursorRequest;
    }
}
