package org.opensearch.graph.unipop.controller.utils;





import javaslang.collection.Stream;

import java.util.Collections;
import java.util.List;

public class CollectionUtil {
    public static <T> List<T> listFromObjectValue(Object value) {
        if (value == null) {
            return Collections.emptyList();
        } else if (Iterable.class.isAssignableFrom(value.getClass())) {
            return Stream.ofAll((Iterable)value).map(o -> o).toJavaList();
        } else if (value.getClass().isArray()) {
            return Stream.of((T[])value).toJavaList();
        } else {
            return Stream.of((T)value).toJavaList();
        }
    }
}
