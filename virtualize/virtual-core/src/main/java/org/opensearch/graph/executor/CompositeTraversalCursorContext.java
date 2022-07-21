package org.opensearch.graph.executor;


import org.opensearch.graph.dispatcher.resource.QueryResource;
import org.opensearch.graph.executor.cursor.TraversalCursorContext;

import java.util.List;

public class CompositeTraversalCursorContext extends TraversalCursorContext {

    private final List<QueryResource> inner;

    public CompositeTraversalCursorContext(TraversalCursorContext outer, List<QueryResource> inner) {
        super(outer.getClient(), outer.getSchemaProvider(), outer.getOntologyProvider(), outer.getOntology(), outer.getQueryResource(), outer.getCursorRequest(), outer.getRuntimeProvision(), outer.getTraversal());
        this.inner = inner;
    }

    @Override
    public CompositeTraversalCursorContext clone() {
        return new CompositeTraversalCursorContext(
                new TraversalCursorContext(getClient(), getSchemaProvider(), getOntologyProvider(), getOntology(), getQueryResource(), getCursorRequest(), getRuntimeProvision(), getTraversal()), getInner());
    }

    public List<QueryResource> getInner() {
        return inner;
    }
}
