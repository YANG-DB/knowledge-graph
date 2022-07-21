package org.opensearch.graph.unipop.controller.common.context;


import org.opensearch.graph.unipop.promise.TraversalConstraint;

import java.util.Optional;

/**
 * Created by Elad on 4/30/2017.
 */
public interface ConstraintContext {
    Optional<TraversalConstraint> getConstraint();
}
