package org.opensearch.graph.unipop.step;





import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;

import java.util.Iterator;
import java.util.Set;

public class NestedStepWrapper<S,E> implements Step<S,E> {

    public NestedStepWrapper(Step<S, E> innerStep, String eType) {
        this.innerStep = innerStep;
        this.eType = eType;
    }


    public String geteType() {
        return eType;
    }

    public Step<S, E> getInnerStep() {
        return innerStep;
    }

    @Override
    public void addStarts(Iterator<Traverser.Admin<S>> starts) {
        innerStep.addStarts(starts);
    }

    @Override
    public void addStart(Traverser.Admin<S> start) {
        innerStep.addStart(start);
    }

    @Override
    public void setPreviousStep(Step<?, S> step) {
        innerStep.setPreviousStep(step);
    }

    @Override
    public Step<?, S> getPreviousStep() {
        return innerStep.getPreviousStep();
    }

    @Override
    public void setNextStep(Step<E, ?> step) {
        innerStep.setNextStep(step);
    }

    @Override
    public Step<E, ?> getNextStep() {
        return innerStep.getNextStep();
    }

    @Override
    public <A, B> Traversal.Admin<A, B> getTraversal() {
        return innerStep.getTraversal();
    }

    @Override
    public void setTraversal(Traversal.Admin<?, ?> traversal) {
        innerStep.setTraversal(traversal);
    }

    @Override
    public void reset() {
        innerStep.reset();
    }

    @Override
    public Step<S, E> clone() {
        return new NestedStepWrapper<>(innerStep, eType);
    }

    @Override
    public Set<String> getLabels() {
        return innerStep.getLabels();
    }

    @Override
    public void addLabel(String label) {
        innerStep.addLabel(label);
    }

    @Override
    public void removeLabel(String label) {
        innerStep.removeLabel(label);
    }

    @Override
    public void setId(String id) {
        innerStep.setId(id);
    }

    @Override
    public String getId() {
        return innerStep.getId();
    }

    @Override
    public boolean hasNext() {
        return innerStep.hasNext();
    }

    @Override
    public Traverser.Admin<E> next() {
        return innerStep.next();
    }

    @Override
    public String toString() {
        return "NestedStepWrapper{" +
                "innerStep=" + innerStep +
                ", eType=" + eType +
                '}';
    }

    private Step<S,E> innerStep;
    private String eType;

}
