package org.opensearch.graph.unipop.controller.common.context;


import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * Created by roman.margolis on 13/09/2017.
 */
public interface BulkContext {
    boolean isEmpty();
    long getBulkSize();
    Iterable<Vertex> getBulkVertices();
    Vertex getVertex(Object id);
}
