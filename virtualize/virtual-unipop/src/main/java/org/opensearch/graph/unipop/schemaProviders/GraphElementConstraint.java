package org.opensearch.graph.unipop.schemaProviders;



import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

public interface GraphElementConstraint {
    Traversal getTraversalConstraint();

    class Impl implements GraphElementConstraint {
        //region Constructors
        public Impl(Traversal traversalConstraint) {
            this.traversalConstraint = traversalConstraint;
        }
        //endregion

        //region GraphElementConstraint Implementation
        @Override
        public Traversal getTraversalConstraint() {
            return this.traversalConstraint;
        }
        //endregion

        //region Fields
        private Traversal traversalConstraint;
        //endregion
    }
}
