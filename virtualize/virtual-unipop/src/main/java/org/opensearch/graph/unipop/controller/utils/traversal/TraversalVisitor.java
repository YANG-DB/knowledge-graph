package org.opensearch.graph.unipop.controller.utils.traversal;





import org.opensearch.graph.unipop.step.BoostingStepWrapper;
import org.opensearch.graph.unipop.step.NestedStepWrapper;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.*;

import java.util.List;

public class TraversalVisitor<TReturn> {
    //Public Methods
    public TReturn visit(Traversal traversal) {
        visitRecursive(traversal);
        return null;
    }
    //endregion

    //Protected Methods
    protected TReturn visitRecursive(Object o) {
        if (Traversal.class.isAssignableFrom(o.getClass())) {
            visitTraversal((Traversal) o);
        } else if (o.getClass() == OrStep.class) {
            visitOrStep((OrStep) o);
        } else if (o.getClass() == AndStep.class) {
            visitAndStep((AndStep) o);
        } else if (o.getClass() == NotStep.class) {
            visitNotStep((NotStep) o);
        } else if (o.getClass() == HasStep.class) {
            visitHasStep((HasStep) o);
        } else if (o.getClass() == WherePredicateStep.class) {
            visitWhereStep((WherePredicateStep) o);
        } else if (o.getClass() == TraversalFilterStep.class) {
            visitTraversalFilterStep((TraversalFilterStep) o);
        } else if(o.getClass() == BoostingStepWrapper.class){
          visitBoostingStep((BoostingStepWrapper) o);
        } else if(o.getClass() == NestedStepWrapper.class){
          visitNestedStep((NestedStepWrapper) o);
        } else {
            //TODO: allow configurable behavior for unsupported or unexpected elements
            throw new UnsupportedOperationException(o.getClass() + " is not supported in promise conditions");
        }

        return null;
    }

    protected TReturn visitBoostingStep(BoostingStepWrapper o) {
        visitRecursive(o.getInnerStep());
        return null;
    }

    protected TReturn visitNestedStep(NestedStepWrapper o) {
        visitRecursive(o.getInnerStep());
        return null;
    }

    protected TReturn visitNotStep(NotStep<?> notStep) {
        notStep.getLocalChildren().forEach(this::visitRecursive);
        return null;
    }

    protected TReturn visitTraversal(Traversal<?, ?> traversal) {
        List<Step> steps = Stream.ofAll(traversal.asAdmin().getSteps()).toJavaList();
        for (Step step : steps) {
            visitRecursive(step);
        }

        return null;
    }

    protected TReturn visitOrStep(OrStep<?> orStep) {
        orStep.getLocalChildren().forEach(this::visitRecursive);

        return null;
    }

    protected TReturn visitAndStep(AndStep<?> andStep) {
        andStep.getLocalChildren().forEach(this::visitRecursive);
        return null;
    }

    protected TReturn visitHasStep(HasStep<?> hasStep) {
        return null;
    }

    protected TReturn visitWhereStep(WherePredicateStep<?> wherePredicateStep) {
        return null;
    }

    protected TReturn visitTraversalFilterStep(TraversalFilterStep<?> traversalFilterStep) {
        return null;
    }
    //endregion
}
