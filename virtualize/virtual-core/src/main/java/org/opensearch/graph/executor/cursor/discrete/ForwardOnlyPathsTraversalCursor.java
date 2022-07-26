package org.opensearch.graph.executor.cursor.discrete;





import org.opensearch.graph.dispatcher.cursor.Cursor;
import org.opensearch.graph.dispatcher.cursor.CursorFactory;
import org.opensearch.graph.executor.cursor.TraversalCursorContext;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.results.AssignmentsQueryResult;
import org.unipop.structure.UniElement;

import java.util.stream.Collectors;

import static org.opensearch.graph.model.results.AssignmentsQueryResult.Builder.instance;


public class ForwardOnlyPathsTraversalCursor extends PathsTraversalCursor {
    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new ForwardOnlyPathsTraversalCursor((TraversalCursorContext) context);
        }
        //endregion
    }
    //endregion

    //region Constructors
    public ForwardOnlyPathsTraversalCursor(TraversalCursorContext context) {
        super(context);
    }
    //endregion

    //region Private Methods
    protected AssignmentsQueryResult toQuery(int numResults) {
        AssignmentsQueryResult.Builder builder = instance();
        final Query pattern = getContext().getQueryResource().getQuery();
        builder.withPattern(pattern);
        (getContext().getTraversal().next(numResults)).forEach(path -> {
            //makes sure no circle exists in path (no getting back - forward only assignments)
            if (path.objects().stream().map(p -> ((UniElement) p).id().toString()).collect(Collectors.toSet()).size() == path.objects().size())
                builder.withAssignment(toAssignment(path));
        });
        return AssignmentsQueryResult.distinct(builder
                .withQueryId(context.getQueryResource().getQueryMetadata().getId())
                .withCursorId(context.getQueryResource().getCurrentCursorId())
                .withTimestamp(context.getQueryResource().getQueryMetadata().getCreationTime())
                .build());
    }

}
