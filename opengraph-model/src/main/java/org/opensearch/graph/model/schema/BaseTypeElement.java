package org.opensearch.graph.model.schema;


import java.util.List;

public interface BaseTypeElement<T> {
    List<T> getNested();

    Props getProps();

    String getMapping();

    String getPartition();

    String getType();
}
