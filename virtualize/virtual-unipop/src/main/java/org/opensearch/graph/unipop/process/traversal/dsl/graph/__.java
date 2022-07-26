package org.opensearch.graph.unipop.process.traversal.dsl.graph;





import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.util.DefaultTraversalStrategies;

public class __ {
    protected __() {
    }

    public static <A> GraphTraversal<A, A> start() {
        return new SearchGraphTraversal<>(new SearchGraphTraversalSource(null, new DefaultTraversalStrategies()));
    }
}
