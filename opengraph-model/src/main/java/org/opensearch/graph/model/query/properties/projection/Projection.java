package org.opensearch.graph.model.query.properties.projection;






import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Created by roman.margolis on 27/02/2018.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "Identity", value = IdentityProjection.class),
        @JsonSubTypes.Type(name = "CalculatedField", value = CalculatedFieldProjection.class)
})
public abstract class Projection {
}
