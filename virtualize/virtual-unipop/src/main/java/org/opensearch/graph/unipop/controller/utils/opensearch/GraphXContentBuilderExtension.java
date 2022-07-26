package org.opensearch.graph.unipop.controller.utils.opensearch;





import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.NamedParameter;
import org.opensearch.common.xcontent.XContentBuilder;
import org.opensearch.common.xcontent.XContentBuilderExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class GraphXContentBuilderExtension implements XContentBuilderExtension {
    @Override
    public Map<Class<?>, XContentBuilder.Writer> getXContentWriters() {
        HashMap<Class<?>, XContentBuilder.Writer> map = new HashMap<>();
        map.put(Character.class, (builder, value) -> builder.value(value.toString()));
        map.put(Constraint.class, (builder, value) -> builder.value(value.toString()));
        map.put(NamedParameter.class, (builder, value) -> builder.value(value.toString()));
        return map;
    }

    @Override
    public Map<Class<?>, XContentBuilder.HumanReadableTransformer> getXContentHumanReadableTransformers() {
        return Collections.emptyMap();
    }

    @Override
    public Map<Class<?>, Function<Object, Object>> getDateTransformers() {
        return Collections.emptyMap();
    }
}
