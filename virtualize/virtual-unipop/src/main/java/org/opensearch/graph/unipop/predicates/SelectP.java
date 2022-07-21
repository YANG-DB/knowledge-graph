package org.opensearch.graph.unipop.predicates;


import org.apache.tinkerpop.gremlin.process.traversal.P;

import java.util.function.BiPredicate;

public enum SelectP implements BiPredicate {
    raw {
        @Override
        public boolean test(Object o, Object o2) {
            return true;
        }
    },
    intern {
        @Override
        public boolean test(Object o, Object o2) {
            return true;
        }
    };

    //region Static
    public static <V> P<V> raw(V name) {
        return new P<V>(SelectP.raw, name);
    }
    public static <V> P<V> intern(V name) { return new P<V>(SelectP.intern, name);
    }
    //endregion
}
