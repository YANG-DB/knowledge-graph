package org.opensearch.graph.unipop.controller.promise.converter;





import org.opensearch.graph.unipop.controller.common.converter.ElementConverter;
import org.opensearch.graph.unipop.promise.Promise;
import org.opensearch.graph.unipop.structure.promise.PromiseFilterEdge;
import org.opensearch.graph.unipop.structure.promise.PromiseVertex;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.opensearch.search.SearchHit;
import org.unipop.process.Profiler;
import org.unipop.structure.UniGraph;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class SearchHitPromiseFilterEdgeConverter implements ElementConverter<SearchHit, Edge> {

    //region Constructor
    public SearchHitPromiseFilterEdgeConverter(UniGraph graph) {
        this.graph = graph;
    }
    //endregion

    @Override
    public Iterable<Edge> convert(SearchHit hit) {
        Map<String, Object> propertiesMap = hit.getSourceAsMap();
        PromiseVertex v = new PromiseVertex(
                Promise.as(hit.getId(), (String) hit.getSourceAsMap().get("type")),
                Optional.empty(),
                graph,
                propertiesMap);

        return Arrays.asList(new PromiseFilterEdge(v, graph));
    }

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
