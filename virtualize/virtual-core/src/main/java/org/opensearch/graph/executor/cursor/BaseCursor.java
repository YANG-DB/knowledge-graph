package org.opensearch.graph.executor.cursor;


import org.opensearch.graph.dispatcher.cursor.Cursor;
import org.opensearch.graph.dispatcher.provision.CursorRuntimeProvision;

public abstract class BaseCursor implements Cursor<TraversalCursorContext> {
    protected TraversalCursorContext context;
    protected CursorRuntimeProvision runtimeProvision;

    public BaseCursor(TraversalCursorContext context) {
        this.context = context;
        this.runtimeProvision = context.getRuntimeProvision();
    }

    @Override
    public int getActiveScrolls() {
        return runtimeProvision.getActiveScrolls();
    }

    @Override
    public boolean clearScrolls() {
        return runtimeProvision.clearScrolls();
    }

    public TraversalCursorContext getContext() {
        return context;
    }

}
