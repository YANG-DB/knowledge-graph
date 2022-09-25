package org.opensearch.graph.unipop.controller.utils.opensearch;

/*-
 * #%L
 * virtual-unipop
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */





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
