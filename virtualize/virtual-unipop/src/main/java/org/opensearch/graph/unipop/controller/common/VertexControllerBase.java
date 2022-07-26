package org.opensearch.graph.unipop.controller.common;





import org.opensearch.graph.unipop.controller.utils.CollectionUtil;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.unipop.process.Profiler;
import org.unipop.query.search.SearchVertexQuery;

import java.util.*;
import java.util.function.Predicate;

public abstract class VertexControllerBase implements SearchVertexQuery.SearchVertexController{
    //region Constructors
    public VertexControllerBase(Predicate<Iterable<String>> applicablePredicate) {
        this(applicablePredicate, Collections.emptySet());
    }

    public VertexControllerBase(Predicate<Iterable<String>> applicablePredicate, Iterable<String> supportedEdgeLabels) {
        this.applicablePredicate = applicablePredicate;
        this.supportedEdgeLabels = Stream.ofAll(supportedEdgeLabels).toJavaSet();
    }
    //endregion

    //region SearchVertexQuery.SearchVertexController Implementation
    @Override
    public Iterator<Edge> search(SearchVertexQuery searchVertexQuery) {
        if (searchVertexQuery.getVertices().size() == 0){
            throw new UnsupportedOperationException("SearchVertexQuery must receive a non-empty list of vertices getTo start with");
        }

        Iterable<String> requestedEdgeLabels = getRequestedEdgeLabels(searchVertexQuery.getPredicates().getPredicates());
        if (!this.applicablePredicate.test(requestedEdgeLabels)) {
            return Collections.emptyIterator();
        }

        return search(searchVertexQuery, getSupportedEdgeLabels(requestedEdgeLabels));
    }

    @Override
    public Profiler getProfiler() {
        return this.profiler;
    }

    @Override
    public void setProfiler(Profiler profiler) {
        this.profiler = profiler;
    }
    //endregion

    //region Protected Methods
    protected Iterable<String> getRequestedEdgeLabels(Iterable<HasContainer> hasContainers) {
        Optional<HasContainer> labelHasContainer =
                Stream.ofAll(hasContainers)
                    .filter(hasContainer -> hasContainer.getKey().equals(T.label.getAccessor()))
                    .toJavaOptional();

        if (!labelHasContainer.isPresent()) {
            return Collections.emptyList();
        }

        List<String> requestedEdgeLabels = CollectionUtil.listFromObjectValue(labelHasContainer.get().getValue());
        return requestedEdgeLabels;
    }

    protected Iterable<String> getSupportedEdgeLabels(Iterable<String> requestEdgeLabels) {
        return Stream.ofAll(requestEdgeLabels)
                .filter(label -> this.supportedEdgeLabels.contains(label))
                .toJavaSet();
    }

    /**
     * implement when searching edge type documents
     * @param searchVertexQuery
     * @param edgeLabels
     * @return
     */
    protected abstract Iterator<Edge> search(SearchVertexQuery searchVertexQuery, Iterable<String> edgeLabels);

    //endregion

    //region Fields
    private Predicate<Iterable<String>> applicablePredicate;
    private Set<String> supportedEdgeLabels;

    protected Profiler profiler;
    //endregion
}
