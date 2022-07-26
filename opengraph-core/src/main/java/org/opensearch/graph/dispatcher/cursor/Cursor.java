package org.opensearch.graph.dispatcher.cursor;







import org.opensearch.graph.dispatcher.provision.CursorRuntimeProvision;
import org.opensearch.graph.model.results.QueryResultBase;

public interface Cursor<T> extends CursorRuntimeProvision {
    QueryResultBase getNextResults(int numResults);
    T getContext();

}
