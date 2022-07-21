package org.opensearch.graph.unipop.controller.utils.labelProvider;



public interface LabelProvider<T> {
    String get(T data);
}
