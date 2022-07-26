package org.opensearch.graph.unipop.controller.utils.traversal;





import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

public interface TraversalValueProvider<T> {
    T getValue(Traversal traversal);
}
