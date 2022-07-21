package org.opensearch.graph.unipop.controller.common.converter;


import java.util.Map;

public interface DataItem {
    Object id();
    Map<String, Object> properties();
}
