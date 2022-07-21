package org.opensearch.graph.model.logical;


import java.util.Map;

public interface Vertex<V extends Vertex> {
    /**
     *
     * @return
     */
    String id();

    /**
     *
     * @return
     */
    String label();

    /**
     *
     * @param label
     * @return
     */
    V label(String label);

    /**
     *
     * @return
     */
    String tag();

    /**
     *
     * @param tag
     * @return
     */
    V tag(String tag);

    /**
     *
     * @param entity
     * @return
     */
    V merge(V entity);

    /**
     *
     * @return
     */
    Map<String,Object> metadata();

    /**
     *
     * @return
     */
    Map<String,Object> fields();

}
