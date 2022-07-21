package org.opensearch.graph.unipop.controller.common.context;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.unipop.promise.TraversalConstraint;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;
import org.opensearch.graph.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.query.StepDescriptor;
import org.unipop.structure.UniGraph;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by roman.margolis on 13/09/2017.
 */
public interface VertexControllerContext extends BulkContext, DirectionContext, ElementControllerContext {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    class Impl extends ElementControllerContext.Impl implements VertexControllerContext {
        //region Constructors
        public Impl(
                UniGraph graph,
                StepDescriptor descriptor,
                ElementType elementType,
                GraphElementSchemaProvider schemaProvider,
                Optional<TraversalConstraint> constraint,
                Iterable<HasContainer> selectPHasContainers,
                int limit,
                Direction direction,
                Iterable<Vertex> bulkVertices) {
            super(graph, descriptor,elementType, schemaProvider, constraint, selectPHasContainers, limit);
            this.direction = direction;
//            this.bulkVertices = Stream.ofAll(bulkVertices).toJavaMap(vertex -> new Tuple2<>(vertex.id(), vertex));
            this.bulkVertices = StreamSupport.stream(bulkVertices.spliterator(),false)
                    .collect(Collectors.toMap(Vertex::id,vertex -> vertex ,(v1,v2) ->{ throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));}
                    ,LinkedHashMap::new));
        }
        //endregion

        //region VertexControllerContext Implementation
        @Override
        public Direction getDirection() {
            return this.direction;
        }

        @Override
        public boolean isEmpty() {
            return bulkVertices.isEmpty();
        }

        @Override
        public long getBulkSize() {
            return bulkVertices.size();
        }

        @Override
        public Iterable<Vertex> getBulkVertices() {
            return bulkVertices.values();
        }

        @Override
        public Vertex getVertex(Object id) {
            return this.bulkVertices.get(id);
        }
        //endregion
        //region Fields
        private Direction direction;
        private Map<Object, Vertex> bulkVertices;
        //endregion
    }
}
