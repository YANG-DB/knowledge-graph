package org.opensearch.graph.unipop.controller.discrete.context;





import org.opensearch.graph.unipop.controller.common.context.VertexControllerContext;
import org.opensearch.graph.unipop.promise.TraversalConstraint;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;
import org.opensearch.graph.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.query.StepDescriptor;
import org.unipop.structure.UniGraph;

import java.util.Optional;

public class DiscreteVertexControllerContext extends VertexControllerContext.Impl {
    public DiscreteVertexControllerContext(UniGraph graph, StepDescriptor stepDescriptor, GraphElementSchemaProvider schemaProvider, Optional<TraversalConstraint> constraint, Iterable<HasContainer> selectPHasContainers, int limit, Direction direction, Iterable<Vertex> bulkVertices) {
        super(graph,stepDescriptor, ElementType.edge, schemaProvider, constraint, selectPHasContainers, limit, direction, bulkVertices);
    }
}
