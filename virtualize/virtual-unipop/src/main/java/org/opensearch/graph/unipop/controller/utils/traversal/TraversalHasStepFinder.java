package org.opensearch.graph.unipop.controller.utils.traversal;



import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class TraversalHasStepFinder implements TraversalValueProvider<Iterable<HasStep>> {
    //region Constructors
    public TraversalHasStepFinder(Predicate<HasStep<?>> hasStepPredicate) {
        this.hasStepPredicate = hasStepPredicate;
    }
    //endregion

    //region TraversalValueProvider Implementation
    @Override
    public Iterable<HasStep> getValue(Traversal traversal) {
        Visitor visitor = new Visitor(this.hasStepPredicate);
        visitor.visit(traversal);
        return visitor.getHasSteps();
    }
    //endregions

    //region Fields
    private Predicate<HasStep<?>> hasStepPredicate;
    //endregion

    //region Visitor
    private class Visitor extends TraversalVisitor<Boolean> {
        //region Constructors
        public Visitor(Predicate<HasStep<?>> predicate) {
            this.predicate = predicate;
            this.hasSteps = new HashSet<>();
        }
        //endregion

        //region Override Methods
        @Override
        protected Boolean visitHasStep(HasStep<?> hasStep) {
            if (this.predicate.test(hasStep)) {
                this.hasSteps.add(hasStep);
                return Boolean.TRUE;
            }

            return Boolean.FALSE;
        }
        //endregion

        //region Properties
        public Iterable<HasStep> getHasSteps() {
            return this.hasSteps;
        }
        //endregion

        //region Fields
        private Predicate<HasStep<?>> predicate;
        private Set<HasStep> hasSteps;
        //endregion
    }
    //endregion
}
