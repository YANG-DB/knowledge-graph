package org.opensearch.graph.executor.cursor.discrete;


import org.opensearch.graph.dispatcher.cursor.Cursor;
import org.opensearch.graph.dispatcher.cursor.CursorFactory;
import org.opensearch.graph.executor.CompositeTraversalCursorContext;
import org.opensearch.graph.executor.cursor.TraversalCursorContext;
import org.opensearch.graph.model.results.*;
import org.opensearch.graph.model.transport.cursor.CreateInnerQueryCursorRequest;

import java.util.*;

public class InnerQueryCursor implements Cursor {
    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new InnerQueryCursor(
                    (CompositeTraversalCursorContext) context,
                    new PathsTraversalCursor((TraversalCursorContext) context),
                    (CreateInnerQueryCursorRequest) context.getCursorRequest());
        }
        //endregion
    }
    //endregion

    //region Constructors
    public InnerQueryCursor(CompositeTraversalCursorContext context, PathsTraversalCursor innerCursor, CreateInnerQueryCursorRequest cursorRequest) {
        this.context = context;
        this.innerCursor = innerCursor;
        this.cursorRequest = cursorRequest;
    }
    //endregion

    //region Cursor Implementation

    @Override
    public QueryResultBase getNextResults(int numResults) {
        AssignmentsQueryResult all = new AssignmentsQueryResult(new ArrayList<>());
        AssignmentsQueryResult nextResult;
        do {
            //todo - implement the following logic
            // 1) for each inner query - drain results (first page ?) -> this step was already done in PageDriver -> createInnerPage()
            // 2) for each assignment in the resulting inner query:
            //      2.1)  - run search plan & generate path traversal for outer query with parameter
            //      2.1.1)  - execute the outer query with the given assignment as parameter to the query
            //      2.2)  - drain all the results and tag the assignments in that collection as related to the parameter

            nextResult = this.innerCursor.getNextResults(numResults);
            all.getAssignments().addAll(nextResult.getAssignments());
        }
        while (nextResult.getSize() > 0);
        return all;
    }

    @Override
    public TraversalCursorContext getContext() {
        return innerCursor.getContext();
    }

    @Override
    public int getActiveScrolls() {
        return innerCursor.getActiveScrolls();
    }

    @Override
    public boolean clearScrolls() {
        return innerCursor.clearScrolls();
    }

    public CreateInnerQueryCursorRequest getCursorRequest() {
        return cursorRequest;
    }

//endregion

    //region Fields
    private CompositeTraversalCursorContext context;
    private PathsTraversalCursor innerCursor;
    private CreateInnerQueryCursorRequest cursorRequest;
    //endregion
}
