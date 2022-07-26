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

import java.util.List;
import java.util.Optional;

public class DiscreteVertexFilterControllerContext  extends VertexControllerContext.Impl {
    //region Constructors
    public DiscreteVertexFilterControllerContext(
            UniGraph graph,
            StepDescriptor stepDescriptor,
            List<Vertex> vertices,
            Optional<TraversalConstraint> constraint,
            List<HasContainer> selectPHasContainers,
            GraphElementSchemaProvider schemaProvider,
            int limit) {
        super(graph,stepDescriptor, ElementType.vertex, schemaProvider, constraint, selectPHasContainers, limit, Direction.OUT, vertices);
    }
    //endregion
}
