package org.opensearch.graph.unipop.controller.utils.traversal;


import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

/**
 * Created by benishue on 27-Mar-17.
 */
public interface TraversalValueProvider<T> {
    T getValue(Traversal traversal);
}
