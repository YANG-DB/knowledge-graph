package org.opensearch.graph.dispatcher.descriptors;







import org.opensearch.graph.model.descriptors.Descriptor;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

public class GraphTraversalDescriptor implements Descriptor<GraphTraversal<?, ?>> {
    @Override
    public String describe(GraphTraversal<?, ?> item) {
        StringBuilder sb = new StringBuilder();
        for(Object step : item.asAdmin().getSteps()) {
            /*if (TraversalParent.class.isAssignableFrom(step.getClass())) {
                TraversalParent traversalParent = (TraversalParent)step;
                traversalParent.getGlobalChildren()
            } else {*/
            sb.append(step.toString());
            //}
            sb.append("\n");
        }
        return sb.toString();
    }
}
