package org.opensearch.graph.unipop.promise;





import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

public interface Constraint {
    static TraversalConstraint by(Traversal traversal) {
        return new TraversalConstraint(traversal);
    }
}
