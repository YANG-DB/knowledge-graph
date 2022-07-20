package org.opensearch.graph.unipop.controller.common.appender;

/*-
 * #%L
 * fuse-dv-unipop
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

import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.unipop.controller.common.context.VertexControllerContext;
import org.opensearch.graph.unipop.controller.search.SearchBuilder;
import org.opensearch.graph.unipop.controller.utils.ElementUtil;
import org.opensearch.graph.unipop.schemaProviders.GraphVertexSchema;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.opensearch.graph.unipop.controller.common.appender.EdgeUtils.getLabel;

/**
 * Created by roman.margolis on 18/10/2017.
 */
public class FilterRoutingSearchAppender implements SearchAppender<VertexControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, VertexControllerContext context) {
        // currently assuming homogeneous vertex bulk
        String vertexLabel = getLabel(context,"?");

        Iterable<GraphVertexSchema> vertexSchemas = context.getSchemaProvider().getVertexSchemas(vertexLabel);
        if (Stream.ofAll(vertexSchemas).isEmpty()) {
            return false;
        }

        // currently supports a single vertex schema
        GraphVertexSchema vertexSchema = Stream.ofAll(vertexSchemas).get(0);

        if (vertexSchema.getRouting().isPresent()) {
            boolean isRoutingFieldFullyAvailable =
                    Stream.ofAll(context.getBulkVertices())
                            .map(vertex -> ElementUtil.<String>value(vertex,
                                    translateRoutingPropertyName(vertexSchema.getRouting().get().getRoutingProperty().getName())))
                            .filter(value -> !value.isPresent())
                            .size() == 0;

            Set<String> routingValues = Collections.emptySet();
            if (isRoutingFieldFullyAvailable) {
               routingValues =
                        Stream.ofAll(context.getBulkVertices())
                                .map(vertex -> ElementUtil.<String>value(vertex,
                                        translateRoutingPropertyName(vertexSchema.getRouting().get().getRoutingProperty().getName())))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .toJavaSet();
            }

            searchBuilder.getRouting().addAll(routingValues);
            return routingValues.size() > 0;
        }

        return false;
    }
    //endregion

    //region Private Methods
    private String translateRoutingPropertyName(String name) {
        return name.equals(GlobalConstants._ID) ? T.id.getAccessor() : name;
    }
    //endregion
}
