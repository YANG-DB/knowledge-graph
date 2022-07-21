package org.opensearch.graph.unipop.controller.utils.idProvider;


import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Map;

public interface EdgeIdProvider<T>  {
    T get(String edgeLabel, Vertex outV, Vertex inV, Map<String, Object> properties);
}
