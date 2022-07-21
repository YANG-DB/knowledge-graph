package org.opensearch.graph.model.transport.cursor;




import org.opensearch.graph.model.transport.CreatePageRequest;

import java.util.Collections;

/**
 * Created by roman.margolis on 11/03/2018.
 */
public class CreateGraphHierarchyCursorRequest extends CreateGraphCursorRequest {
    public static final String CursorType = "graphHierarchy";

    //region Constructors
    public CreateGraphHierarchyCursorRequest() {
        this(Collections.emptyList());
    }

    public CreateGraphHierarchyCursorRequest(Iterable<String> countTags) {
        this(countTags, null);
    }

    public CreateGraphHierarchyCursorRequest(Iterable<String> countTags, CreatePageRequest createPageRequest) {
        this(Include.all, countTags, createPageRequest);
    }

    public CreateGraphHierarchyCursorRequest(Iterable<String> countTags, CreatePageRequest createPageRequest,GraphFormat format) {
        this(Include.all, countTags, createPageRequest,format);
    }

    public CreateGraphHierarchyCursorRequest(Include include, Iterable<String> countTags, CreatePageRequest createPageRequest) {
        super(CursorType, include, createPageRequest);
        this.countTags = countTags;
    }

    public CreateGraphHierarchyCursorRequest(Include include, Iterable<String> countTags, CreatePageRequest createPageRequest,GraphFormat format) {
        super(CursorType, include, createPageRequest,format);
        this.countTags = countTags;
    }
    //endregion

    //region Properties
    public Iterable<String> getCountTags() {
        return countTags;
    }

    public void setCountTags(Iterable<String> countTags) {
        this.countTags = countTags;
    }
    //endregion

    //region Fields
    private Iterable<String> countTags;
    //endregion
}
