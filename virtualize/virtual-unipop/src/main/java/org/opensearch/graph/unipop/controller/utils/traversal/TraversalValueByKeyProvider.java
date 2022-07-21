package org.opensearch.graph.unipop.controller.utils.traversal;


import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

import java.util.Set;

/**
 * Created by benishue on 27-Mar-17.
 */
public interface TraversalValueByKeyProvider<T> {
    Set<String> getValueByKey(Traversal traversal, String key);
}
