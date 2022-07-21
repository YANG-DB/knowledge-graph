package org.opensearch.graph.unipop.promise;


import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

/**
 * Created by lior.perry on 07/03/2017.
 */
public class TraversalConstraint extends TraversalPromise implements Constraint {
    //region Static
    public static TraversalConstraint EMPTY = new TraversalConstraint(__.start());
    //endregion

    //region Constructor
    public TraversalConstraint(Traversal traversal) {
        super(null, traversal);
    }
    //endregion

    //region Override Methods
    @Override
    public String toString() {
        return "Constraint.by(" + this.getTraversal().toString() + ")";
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }

        if (!(other instanceof TraversalConstraint)) {
            return false;
        }

        return this.getTraversal().equals(((TraversalConstraint)other).getTraversal());
    }

    @Override
    public int hashCode() {
        return getTraversal().hashCode();
    }
    //endregion
}
