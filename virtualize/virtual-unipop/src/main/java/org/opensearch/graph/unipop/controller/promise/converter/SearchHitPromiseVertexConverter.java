package org.opensearch.graph.unipop.controller.promise.converter;





import org.opensearch.graph.unipop.controller.common.converter.ElementConverter;
import org.opensearch.graph.unipop.promise.Promise;
import org.opensearch.graph.unipop.structure.promise.PromiseVertex;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.opensearch.search.SearchHit;
import org.unipop.process.Profiler;
import org.unipop.structure.UniGraph;

import java.util.Collections;
import java.util.Optional;

public class SearchHitPromiseVertexConverter implements ElementConverter<SearchHit, Element> {
    //region Constructor
    public SearchHitPromiseVertexConverter(UniGraph graph) {
        this.graph = graph;
    }
    //endregion

    @Override
    public Iterable<Element> convert(SearchHit element) {
        return Collections.singletonList(
                new PromiseVertex(
                    Promise.as(element.getId(), (String) element.getSourceAsMap().get("type")),
                    Optional.empty(),
                    graph,
                    element.getSourceAsMap()));
    }
    //endregion

    //endregion
    @Override
    public Profiler getProfiler() {
        return this.profiler;
    }

    @Override
    public void setProfiler(Profiler profiler) {
        this.profiler = profiler;
    }

    //region Fields
    private Profiler profiler = Profiler.Noop.instance ;
    private UniGraph graph;
    //endregion
}
