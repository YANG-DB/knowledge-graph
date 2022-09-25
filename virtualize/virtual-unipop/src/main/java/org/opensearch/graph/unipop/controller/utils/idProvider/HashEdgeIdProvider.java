package org.opensearch.graph.unipop.controller.utils.idProvider;

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





import org.opensearch.graph.unipop.controller.utils.traversal.TraversalHashProvider;
import org.opensearch.graph.unipop.promise.TraversalConstraint;
import org.opensearch.graph.unipop.promise.TraversalPromise;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Map;
import java.util.Optional;

public class HashEdgeIdProvider implements EdgeIdProvider<String> {
    //region Constructors
    public HashEdgeIdProvider(Optional<TraversalConstraint> constraint) throws Exception {
        this.traversalHashProvider = new TraversalHashProvider(Object::toString, "MD5", 8);
        this.constraintHash = this.traversalHashProvider
                .getValue(constraint.map(TraversalPromise::getTraversal)
                .orElseGet(() -> TraversalConstraint.EMPTY.getTraversal()));
    }
    //endregion

    //region EdgeIdProvider Implementation
    @Override
    public String get(String edgeLabel, Vertex outV, Vertex inV, Map<String, Object> properties) {
        return new StringBuilder(edgeLabel)
                .append(outV.id())
                .append(inV.id())
                //.append(this.constraintHash)
                .toString();

        /*return this.traversalHashProvider.getValue(
                __.start().and(
                        __.start().has(T.label, edgeLabel),
                        __.start().has("outV.id", outV.id()),
                        __.start().has("inV.id", inV.id()),
                        __.start().has("constraintHash", constraintHash)));*/
    }
    //endregion

    //region Fields
    private String constraintHash;
    private TraversalHashProvider traversalHashProvider;
    //endregion
}
