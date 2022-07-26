package org.opensearch.graph.unipop.controller.common.context;





import org.apache.tinkerpop.gremlin.structure.Vertex;

public interface BulkContext {
    boolean isEmpty();
    long getBulkSize();
    Iterable<Vertex> getBulkVertices();
    Vertex getVertex(Object id);
}
