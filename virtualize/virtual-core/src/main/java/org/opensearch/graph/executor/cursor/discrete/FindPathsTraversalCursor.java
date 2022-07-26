package org.opensearch.graph.executor.cursor.discrete;





import org.opensearch.graph.dispatcher.cursor.Cursor;
import org.opensearch.graph.dispatcher.cursor.CursorFactory;
import org.opensearch.graph.executor.cursor.TraversalCursorContext;
import org.opensearch.graph.model.results.Assignment;
import org.opensearch.graph.model.results.AssignmentsQueryResult;
import org.opensearch.graph.model.transport.cursor.FindPathTraversalCursorRequest;

import static org.opensearch.graph.model.results.AssignmentsQueryResult.Builder.instance;


public class FindPathsTraversalCursor extends PathsTraversalCursor {

    private final int amount;

    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new FindPathsTraversalCursor((TraversalCursorContext) context,
                    ((FindPathTraversalCursorRequest) context.getCursorRequest()).getAmount());
        }
        //endregion
    }
    //endregion

    //region Constructors
    public FindPathsTraversalCursor(TraversalCursorContext context, int amount) {
        super(context);
        this.amount = amount;
    }
    //endregion

    //region Private Methods
    protected AssignmentsQueryResult toQuery(int numResults) {
        AssignmentsQueryResult.Builder builder = instance();
        //
        (getContext().getTraversal().next(amount)).forEach(path -> {
            Assignment assignments = toAssignment(path);
            builder.withAssignment(assignments);
        });
        return AssignmentsQueryResult.distinct(builder.build());
    }

}
