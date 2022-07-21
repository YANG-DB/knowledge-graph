package org.opensearch.graph.unipop.controller.utils.idProvider;


import org.opensearch.graph.unipop.controller.utils.traversal.TraversalHashProvider;
import org.opensearch.graph.unipop.promise.TraversalConstraint;
import org.opensearch.graph.unipop.promise.TraversalPromise;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Map;
import java.util.Optional;

/**
 * Created by Roman on 15/05/2017.
 */
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
