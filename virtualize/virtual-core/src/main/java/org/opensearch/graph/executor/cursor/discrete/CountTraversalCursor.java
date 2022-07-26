package org.opensearch.graph.executor.cursor.discrete;





import org.opensearch.graph.dispatcher.cursor.Cursor;
import org.opensearch.graph.dispatcher.cursor.CursorFactory;
import org.opensearch.graph.executor.cursor.TraversalCursorContext;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.results.AssignmentCount;
import org.opensearch.graph.model.results.AssignmentsQueryResult;
import org.apache.tinkerpop.gremlin.structure.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.opensearch.graph.model.results.AssignmentsQueryResult.Builder.instance;
import static java.util.stream.Collectors.groupingBy;

public class CountTraversalCursor extends PathsTraversalCursor {
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new CountTraversalCursor((TraversalCursorContext) context);
        }
        //endregion
    }

    public CountTraversalCursor(TraversalCursorContext context) {
        super(context);
    }

    @Override
    public AssignmentsQueryResult getNextResults(int numResults) {
        return super.getNextResults(numResults);
    }

    protected AssignmentsQueryResult toQuery(int numResults) {
        AssignmentsQueryResult.Builder builder = instance();
        final Query pattern = getContext().getQueryResource().getQuery();
        builder.withPattern(pattern);
        Map<String, AtomicLong> labelsCount = new HashMap<>();
        //build assignments
        if (!getContext().getTraversal().hasNext()) {
            labelsCount.put("Elements",new AtomicLong(0));
        } else {
            while (getContext().getTraversal().hasNext()) {
                (getContext().getTraversal().next(numResults)).forEach(path -> {
                    Map<String, Long> collect = path.objects().stream().map(e -> (Element) e)
                            .collect(groupingBy(Element::label, Collectors.counting()));

                    collect.forEach((key, value) -> {
                        if (labelsCount.containsKey(key))
                            labelsCount.get(key).addAndGet(value);
                        else
                            labelsCount.put(key, new AtomicLong(value));
                    });
                });
            }
        }
        return builder.withAssignment(new AssignmentCount(labelsCount))
                .withPattern(context.getQueryResource().getQuery())
                .withCursorId(context.getQueryResource().getCurrentCursorId())
                .withQueryId(context.getQueryResource().getQueryMetadata().getId())
                .withTimestamp(context.getQueryResource().getQueryMetadata().getCreationTime())
                .build();
    }
}
