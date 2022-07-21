package org.opensearch.graph.unipop.process.traversal.traverser;



import org.apache.tinkerpop.gremlin.process.traversal.TraverserGenerator;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserGeneratorFactory;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;

import java.util.Set;

public class ThinPathTraverserGeneratorFactory implements TraverserGeneratorFactory {
    //region TraverserGeneratorFactory Implementation
    @Override
    public TraverserGenerator getTraverserGenerator(Set<TraverserRequirement> set) {
        return new ThinPathTraverserGenerator();
    }
    //endregion
}
