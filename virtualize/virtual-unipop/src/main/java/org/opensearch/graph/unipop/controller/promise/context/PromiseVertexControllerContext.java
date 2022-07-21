package org.opensearch.graph.unipop.controller.promise.context;



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

public class PromiseVertexControllerContext extends VertexControllerContext.Impl {
    public PromiseVertexControllerContext(UniGraph graph, StepDescriptor stepDescriptor, GraphElementSchemaProvider schemaProvider, Optional<TraversalConstraint> constraint, Iterable<HasContainer> selectPHasContainers, int limit, Iterable<Vertex> bulkVertices) {
        super(graph,stepDescriptor, ElementType.edge, schemaProvider, constraint, selectPHasContainers, limit, Direction.OUT, bulkVertices);
    }
}
