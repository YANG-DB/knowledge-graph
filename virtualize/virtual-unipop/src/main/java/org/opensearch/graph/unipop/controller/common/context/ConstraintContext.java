package org.opensearch.graph.unipop.controller.common.context;



import org.opensearch.graph.unipop.promise.TraversalConstraint;

import java.util.Optional;

public interface ConstraintContext {
    Optional<TraversalConstraint> getConstraint();
}
