package org.opensearch.graph.model.logical;





import com.fasterxml.jackson.annotation.*;
import org.opensearch.graph.model.ontology.EntityType;
import org.opensearch.graph.model.results.Property;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogicalNode implements Vertex<LogicalNode> {
    public static final String NODE = "Node";

    @JsonProperty("label")
    private String label = NODE;
    @JsonProperty("id")
    private String id;

    @JsonProperty("tag")
    private String tag;

    @JsonProperty("metadata")
    private NodeMetadata metadata = new NodeMetadata();
    @JsonProperty("properties")
    private NodeProperties properties = new NodeProperties();

    public LogicalNode() {}

    public LogicalNode(String id, String label) {
        this.id = id;
        this.label = label;
    }

    @Override
    @JsonProperty("label")
    public LogicalNode label(String label) {
        setLabel(label);
        return this;
    }

    @Override
    public LogicalNode tag(String tag) {
        setTag(tag);
        return this;
    }

    @Override
    @JsonProperty("tag")
    public String tag() {
        return getTag();
    }

    @JsonProperty("tag")
    public String getTag() {
        return tag;
    }

    @JsonProperty("tag")
    public void setTag(String tag) {
        this.tag = tag;
    }

    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("metadata")
    public NodeMetadata getMetadata() {
        return metadata;
    }

    @JsonProperty("properties")
    public NodeProperties getProperties() {
        return properties;
    }

    @Override
    @JsonProperty("id")
    public String id() {
        return getId();
    }

    @Override
    @JsonProperty("label")
    public String label() {
        return getLabel();
    }

    @Override
    public LogicalNode merge(LogicalNode entity) {
        //todo merge
        return this;
    }

    @JsonProperty("label")
    public void setLabel(String label) {
        this.label = label;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("metadata")
    public void setMetadata(NodeMetadata metadata) {
        this.metadata = metadata;
    }

    @JsonProperty("properties")
    public void setProperties(NodeProperties properties) {
        this.properties = properties;
    }

    @Override
    @JsonProperty("metadata")
    public Map<String, Object> metadata() {
        return getMetadata().getProperties();
    }

    @Override
    @JsonProperty("properties")
    public Map<String, Object> fields() {
        return getProperties().getProperties();
    }

    @JsonIgnore
    public Object getProperty(String partition) {
        return getProperties().properties.get(partition);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id='" + id + '\'' +
                "label='" + label + '\'' +
                ", metadata=" + metadata +  '\'' +
                ", tag=" + tag +  '\'' +
                ", properties=" + properties +
                '}';
    }

    @JsonIgnore
    public LogicalNode withMetadata(Collection<Property> properties) {
        properties.forEach(p -> this.metadata.addProperties(p.getpType(), p.getValue()));
        return this;
    }

    @JsonIgnore
    public LogicalNode withProperty(String property, Object value) {
        properties.addProperties(property, value);
        return this;
    }

    @JsonIgnore
    public LogicalNode withProperty(EntityType type, String property, Object value) {
        if (type.containsProperty(property)) {
            properties.addProperties(property, value);
        } else {
            //add property with _underscore so it can be ignored if needed
            properties.addProperties(String.format("_%s", property), value);
        }
        return this;
    }

    @JsonIgnore
    public Optional<Object> getPropertyValue(String name) {
        return properties.properties.containsKey(name) ? Optional.of(properties.properties.get(name)) : Optional.empty();
    }

    public LogicalNode withMetadata(EntityType type, Collection<Property> properties) {
        properties.stream().filter(p -> type.containsProperty(p.getpType()))
                .forEach(p -> this.properties.addProperties(p.getpType(), p.getValue()));
        return this;
    }

    public LogicalNode withProperties(List<Property> properties) {
        properties.forEach(p -> this.properties.addProperties(p.getpType(), p.getValue()));
        return this;
    }

    public LogicalNode withTag(String tag) {
        this.tag = tag;
        return this;
    }


    public static class NodeMetadata implements PropertyFields<NodeMetadata> {
        private Map<String, Object> properties = new HashMap<>();


        @JsonAnyGetter
        public Map<String, Object> getProperties() {
            return properties;
        }

        @JsonAnySetter
        public NodeMetadata addProperties(String key, Object value) {
            this.properties.put(key, value);
            return this;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{" +
                    ", properties=" + properties +
                    '}';
        }
    }

    public static class NodeProperties implements PropertyFields<NodeProperties> {
        private Map<String, Object> properties = new HashMap<>();

        @JsonAnyGetter
        public Map<String, Object> getProperties() {
            return properties;
        }

        @JsonAnySetter
        public NodeProperties addProperties(String key, Object value) {
            this.properties.put(key, value);
            return this;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{" +
                    "properties=" + properties +
                    '}';
        }
    }
}
