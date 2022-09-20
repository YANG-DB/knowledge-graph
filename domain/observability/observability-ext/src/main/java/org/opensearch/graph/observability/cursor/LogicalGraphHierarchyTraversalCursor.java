package org.opensearch.graph.observability.cursor;

/*-
 * #%L
 * dragons-ext
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */



import org.opensearch.graph.dispatcher.cursor.Cursor;
import org.opensearch.graph.dispatcher.cursor.CursorFactory;
import org.opensearch.graph.executor.cursor.BaseCursor;
import org.opensearch.graph.executor.cursor.TraversalCursorContext;
import org.opensearch.graph.executor.cursor.discrete.NewGraphHierarchyTraversalCursor;
import org.opensearch.graph.model.results.QueryResultBase;
import org.opensearch.graph.model.transport.cursor.CreateGraphCursorRequest.GraphFormat;
import org.opensearch.graph.model.transport.cursor.LogicalGraphCursorRequest;

public class LogicalGraphHierarchyTraversalCursor extends BaseCursor {

    private Cursor<TraversalCursorContext> innerCursor;
    private GraphFormat format;

    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new LogicalGraphHierarchyTraversalCursor(
                    (TraversalCursorContext) context,
                    ((LogicalGraphCursorRequest) context.getCursorRequest()).getCountTags(),
                    ((LogicalGraphCursorRequest) context.getCursorRequest()).getFormat());
        }
        //endregion
    }

    public LogicalGraphHierarchyTraversalCursor(TraversalCursorContext context, Iterable<String> countTags, GraphFormat format) {
        super(context);
        this.format = format;
        innerCursor = new NewGraphHierarchyTraversalCursor(context,countTags);
    }

    @Override
    public QueryResultBase getNextResults(int numResults) {
        return innerCursor.getNextResults(numResults);
    }

    @Override
    public TraversalCursorContext getContext() {
        return innerCursor.getContext();
    }
}
