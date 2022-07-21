package org.opensearch.graph.unipop.schemaProviders;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.IndexPartitions;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Optional;

/**
 * Created by roman on 1/16/2015.
 */
public interface GraphVertexSchema extends GraphElementSchema {
    default Class getSchemaElementType() {
        return Vertex.class;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    class Impl extends GraphElementSchema.Impl implements GraphVertexSchema {
        //region Constructors
        public Impl(String label ) {
            super(label);
        }

        public Impl(String label, GraphElementRouting routing) {
            super(label, routing);
        }

        public Impl(String label, IndexPartitions indexPartitions) {
            super(label, indexPartitions);
        }

        public Impl(String label, GraphElementRouting routing, IndexPartitions indexPartitions) {
            super(label, routing, indexPartitions);
        }

        public Impl(String label, IndexPartitions indexPartitions, Iterable<GraphElementPropertySchema> properties) {
            super(label, indexPartitions, properties);
        }

        public Impl(String label,
                    GraphElementConstraint constraint,
                    Optional<GraphElementRouting> routing,
                    Optional<IndexPartitions> indexPartitions,
                    Iterable<GraphElementPropertySchema> properties) {
            super(label, constraint, routing, indexPartitions, properties);
        }
        //endregion
    }
}
