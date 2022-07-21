package org.opensearch.graph.executor.cursor.discrete.mock;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.graph.dispatcher.cursor.Cursor;
import org.opensearch.graph.dispatcher.cursor.CursorFactory;
import org.opensearch.graph.executor.cursor.BaseCursor;
import org.opensearch.graph.executor.cursor.TraversalCursorContext;
import org.opensearch.graph.model.results.AssignmentsQueryResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class PathsTraversalCursor extends BaseCursor {
    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new PathsTraversalCursor(
                    (TraversalCursorContext)context,
                    Paths.get(System.getProperty("user.dir"), "mockResults").toString());
        }
        //endregion
    }
    //endregion

    //region Constructors
    public PathsTraversalCursor(TraversalCursorContext context, String mockResultsFolder) {
        super(context);
        this.mockResultsFolder = mockResultsFolder;
        this.mapper = new ObjectMapper();
    }
    //endregion

    //region Cursor Implementation
    @Override
    public AssignmentsQueryResult getNextResults(int numResults) {
        String queryName = context.getQueryResource().getQuery().getName();
        try {
            return this.mapper.readValue(new File(Paths.get(this.mockResultsFolder, queryName + ".json").toString()), AssignmentsQueryResult.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new AssignmentsQueryResult();
        }
    }
    //endregion

    //region Fields
    private String mockResultsFolder;
    private ObjectMapper mapper;
    //endregion
}
