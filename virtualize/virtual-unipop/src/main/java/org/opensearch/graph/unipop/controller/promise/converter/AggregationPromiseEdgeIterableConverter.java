package org.opensearch.graph.unipop.controller.promise.converter;


import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.unipop.controller.utils.idProvider.EdgeIdProvider;
import org.opensearch.graph.unipop.controller.utils.labelProvider.LabelProvider;
import org.opensearch.graph.unipop.controller.common.converter.ElementConverter;
import org.opensearch.graph.unipop.promise.Promise;
import org.opensearch.graph.unipop.structure.promise.PromiseEdge;
import org.opensearch.graph.unipop.structure.promise.PromiseVertex;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.opensearch.search.aggregations.Aggregation;
import org.opensearch.search.aggregations.bucket.terms.Terms;
import org.unipop.process.Profiler;
import org.unipop.structure.UniGraph;

import java.util.*;

public class AggregationPromiseEdgeIterableConverter implements ElementConverter<Map<String, Aggregation>, Iterator<Edge>> {
    //region Constructor
    public AggregationPromiseEdgeIterableConverter(
            UniGraph graph,
            EdgeIdProvider<String> edgeIdProvider,
            LabelProvider<String> vertexLabelProvider) {
        this.graph = graph;
        this.edgeIdProvider = edgeIdProvider;
        this.vertexLabelProvider = vertexLabelProvider;
    }
    //endregion


    @Override
    public Iterable<Iterator<Edge>> convert(Map<String, Aggregation> aggMap) {
        ArrayList<Edge> edges = new ArrayList<>();

        Terms layer1 = (Terms)aggMap.get(GlobalConstants.EdgeSchema.SOURCE);
        layer1.getBuckets().forEach(b -> {
            String sourceId = b.getKeyAsString();
            String sourceLabel = this.vertexLabelProvider.get(sourceId);
            PromiseVertex sourceVertex = new PromiseVertex(Promise.as(sourceId, sourceLabel),Optional.empty(),graph);

            Terms layer2 = (Terms) b.getAggregations().asMap().get(GlobalConstants.EdgeSchema.DEST);
            layer2.getBuckets().forEach(innerBucket -> {
                String destId = innerBucket.getKeyAsString();
                String destLabel = this.vertexLabelProvider.get(destId);

                PromiseVertex destVertex = new PromiseVertex(Promise.as(destId, destLabel), Optional.empty(), graph);

                Map<String,Object> propMap = new HashMap<>();
                propMap.put(GlobalConstants.HasKeys.COUNT, innerBucket.getDocCount());

                PromiseEdge promiseEdge = new PromiseEdge(
                        edgeIdProvider.get(
                                GlobalConstants.Labels.PROMISE,
                                sourceVertex,
                                destVertex,
                                propMap),
                        sourceVertex,
                        destVertex,
                        destVertex,
                        propMap,
                        graph);

                edges.add(promiseEdge);
            });
        });

        return Arrays.asList(edges.iterator());

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
    private EdgeIdProvider<String> edgeIdProvider;
    private LabelProvider<String> vertexLabelProvider;
    //endregion
}
