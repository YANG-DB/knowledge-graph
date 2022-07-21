package org.opensearch.graph.unipop.controller.discrete.converter;



import org.opensearch.graph.unipop.controller.common.context.VertexControllerContext;
import org.opensearch.graph.unipop.controller.common.converter.ElementConverter;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import org.opensearch.graph.unipop.schemaProviders.GraphVertexSchema;
import org.opensearch.graph.unipop.structure.discrete.DiscreteEdge;
import org.opensearch.graph.unipop.structure.discrete.DiscreteVertex;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.opensearch.search.SearchHit;
import org.unipop.process.Profiler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DiscreteVertexFilterConverter implements ElementConverter<SearchHit, Edge> {
    //region Constructor
    public DiscreteVertexFilterConverter(VertexControllerContext context,Profiler profiler) {
        this.context = context;
        this.typeToLabelVertexSchemas = Stream.ofAll(context.getSchemaProvider().getVertexSchemas())
                .toJavaMap(vertexSchema ->
                        new Tuple2<>(
                                Stream.ofAll(new TraversalValuesByKeyProvider().getValueByKey(
                                        vertexSchema.getConstraint().getTraversalConstraint(),
                                        T.label.getAccessor()))
                                        .get(0),
                                vertexSchema));
        this.profiler = profiler;
        context.getStepDescriptor().getDescription()
                .ifPresent(v->this.profiler.get().setCount(v,0));
    }
    //endregion

    //region ElementConverter Implementation
    @Override
    public Iterable<Edge> convert(SearchHit hit) {
        Vertex contextVertex = context.getVertex(hit.getId());
        Map<String, Object> contextVertexProperties =
                Stream.ofAll(contextVertex::properties).toJavaMap(property -> new Tuple2<>(property.key(), property.value()));

        //        ##Es 5 optimization for searchHit deprecated Logger
        //        Map<String, Object> source = SearchHitUtils.convertToMap(hit);
        Map<String, Object> source = new HashMap<>(hit.getSourceAsMap());
        contextVertexProperties.putAll(source);

        String label = this.typeToLabelVertexSchemas.get(source.get("type")).getLabel();
        String stepName = context.getStepDescriptor().getDescription().orElse(label);
        profiler.get().incrementCount(stepName,1);

        DiscreteVertex v = new DiscreteVertex(
                hit.getId(),
                label,
                context.getGraph(),
                contextVertexProperties);

        return Collections.singletonList(new DiscreteEdge(
                v.id(),
                GlobalConstants.Labels.PROMISE_FILTER,
                v,
                v,
                v,
                context.getGraph(),
                Collections.emptyMap()));
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
    private VertexControllerContext context;
    private Map<String, GraphVertexSchema> typeToLabelVertexSchemas;
    //endregion
}
