package org.unipop.schema.property.type;







import org.apache.tinkerpop.gremlin.process.traversal.P;

import java.util.function.BiPredicate;


public interface PropertyType {
    String getType();
    default <V> P<V> translate(P<V> predicate){
        return predicate;
    }
}
