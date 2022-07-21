package org.opensearch.graph.unipop.controller.utils.traversal;



import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

import java.util.Set;

public interface TraversalValueByKeyProvider<T> {
    Set<String> getValueByKey(Traversal traversal, String key);
}
