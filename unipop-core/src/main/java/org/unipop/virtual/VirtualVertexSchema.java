package org.unipop.virtual;








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
