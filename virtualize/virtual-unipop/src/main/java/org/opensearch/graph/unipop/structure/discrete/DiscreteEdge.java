package org.opensearch.graph.unipop.structure.discrete;





import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.structure.UniEdge;
import org.unipop.structure.UniGraph;

import java.util.Map;

public class DiscreteEdge extends UniEdge {
    //region Constructors
    public DiscreteEdge(Object id, String label, Vertex outV, Vertex inV, Vertex otherVertex, UniGraph graph, Map<String, Object> properties) {
        super(properties, outV, inV, otherVertex, graph);
        this.id = id.toString();
        this.label = label;
    }

    //endregion
}
