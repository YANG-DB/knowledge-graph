package org.opensearch.graph.unipop.controller.discrete.context;


import org.opensearch.graph.unipop.controller.common.context.ElementControllerContext;
import org.opensearch.graph.unipop.promise.TraversalConstraint;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;
import org.opensearch.graph.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.unipop.query.StepDescriptor;
import org.unipop.structure.UniGraph;

import java.util.Optional;

/**
 * Created by roman.margolis on 12/09/2017.
 */
public class DiscreteElementControllerContext extends ElementControllerContext.Impl {
    public DiscreteElementControllerContext(UniGraph graph, StepDescriptor stepDescriptor, ElementType elementType, GraphElementSchemaProvider schemaProvider, Optional<TraversalConstraint> constraint, Iterable<HasContainer> selectPHasContainers, int limit) {
        super(graph, stepDescriptor,elementType, schemaProvider, constraint, selectPHasContainers, limit);
    }
}
