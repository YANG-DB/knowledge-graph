package org.opensearch.graph.unipop.promise;

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





import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

public class TraversalConstraint extends TraversalPromise implements Constraint {
    //region Static
    public static TraversalConstraint EMPTY = new TraversalConstraint(__.start());
    //endregion

    //region Constructor
    public TraversalConstraint(Traversal traversal) {
        super(null, traversal);
    }
    //endregion

    //region Override Methods
    @Override
    public String toString() {
        return "Constraint.by(" + this.getTraversal().toString() + ")";
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }

        if (!(other instanceof TraversalConstraint)) {
            return false;
        }

        return this.getTraversal().equals(((TraversalConstraint)other).getTraversal());
    }

    @Override
    public int hashCode() {
        return getTraversal().hashCode();
    }
    //endregion
}
