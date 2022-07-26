package org.opensearch.graph.unipop.controller.discrete.converter;





import org.opensearch.graph.unipop.controller.common.context.ElementControllerContext;
import org.opensearch.graph.unipop.controller.common.converter.ElementConverter;
import org.opensearch.graph.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import org.opensearch.graph.unipop.schemaProviders.GraphVertexSchema;
import org.opensearch.graph.unipop.structure.discrete.DiscreteVertex;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.T;
import org.opensearch.search.SearchHit;
import org.unipop.process.Profiler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DiscreteVertexConverter<E extends Element> implements ElementConverter<SearchHit, E> {
    //region Constructors
    public DiscreteVertexConverter(ElementControllerContext context,Profiler profiler) {
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
    public Iterable<E> convert(SearchHit searchHit) {
        Map<String, Object> source = new HashMap<>(searchHit.getSourceAsMap());
        //        ##Es 5 optimization for searchHit deprecated Logger
        //        Map<String, Object> source = SearchHitUtils.convertToMap(searchHit);

        if(searchHit.getScore() > 0){
            source.put("score", searchHit.getScore());
        }

        String label = this.typeToLabelVertexSchemas.get(source.get("type")).getLabel();
        String stepName = context.getStepDescriptor().getDescription().orElse(label);
        profiler.get().incrementCount(stepName,1);
        return Arrays.asList((E)new DiscreteVertex(
                searchHit.getId(),
                label,
                context.getGraph(),
                source));
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
    private ElementControllerContext context;
    private Map<String, GraphVertexSchema> typeToLabelVertexSchemas;
    //endregion
}
