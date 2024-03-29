package org.opensearch.graph.unipop.predicates;

/*-
 * #%L
 * virtual-unipop
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */





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
