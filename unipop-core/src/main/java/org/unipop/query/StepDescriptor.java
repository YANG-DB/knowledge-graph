package org.unipop.query;








import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.util.MutableMetrics;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalMetrics;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.unipop.process.UniBulkStep;
import org.unipop.process.UniPredicatesStep;
import org.unipop.process.start.UniGraphStartStep;
import org.unipop.process.vertex.UniGraphVertexStep;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

public class StepDescriptor {

    private MutableMetrics metrics;
    private Step step;

    public StepDescriptor(Step step) {
        this.step = step;
    }

    public <S, E extends Element> StepDescriptor(Step<S, E> step, MutableMetrics metrics) {
        this(step);
        this.metrics = metrics;
    }

    public String getId(){
        return step.getId();
    }

    public Set<String> getLabels(){
        return step.getLabels();
    }

    public Optional<String> getDescription(){
        Optional<String> name = step.getLabels().stream().findFirst();
        if(name.isPresent())
            return name;

        if(step instanceof UniPredicatesStep) {
            Optional<String> nextLabel = (step).getNextStep().getLabels().stream().findFirst();
            if(nextLabel.isPresent()) return Optional.of("Predicate["+nextLabel.get()+"]");

            Optional<String> previousLabel = (step).getPreviousStep().getLabels().stream().findFirst();
            if(previousLabel.isPresent()) return Optional.of("Predicate["+previousLabel.get()+"]");
        }
        return Optional.empty();
    }

    public Optional<MutableMetrics> getMetrics(){
        return Optional.ofNullable(metrics);
    }

    @Override
    public String toString() {
        return step.toString()
                + " { ID: " + step.getId()+ " }";
    }
}
