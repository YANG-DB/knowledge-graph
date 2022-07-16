package org.unipop.virtual;

/*-
 * #%L
 * unipop-core
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




import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.json.JSONObject;
import org.unipop.schema.element.AbstractElementSchema;
import org.unipop.schema.element.VertexSchema;
import org.unipop.structure.UniGraph;
import org.unipop.structure.UniVertex;

import java.util.Map;

public class VirtualVertexSchema extends AbstractElementSchema<Vertex> implements VertexSchema{

    public VirtualVertexSchema(JSONObject configuration, UniGraph graph) {
        super(configuration, graph);
//        propertySchemas.forEach(schema -> {
//            if (schema instanceof NonDynamicPropertySchema)
//                schema.excludeDynamicProperties().add(T.id.getAccessor());
//        });
//        addPropertySchema(T.id.getAccessor(), "@" + T.id.getAccessor());
    }

    @Override
    public String getFieldByPropertyKey(String key) {
        return null;
    }

    @Override
    public Vertex createElement(Map<String, Object> fields) {
        Map<String, Object> properties = getProperties(fields);
        return new UniVertex(properties, graph);
    }
}
