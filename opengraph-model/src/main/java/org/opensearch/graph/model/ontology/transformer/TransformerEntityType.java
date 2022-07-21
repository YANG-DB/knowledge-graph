package org.opensearch.graph.model.ontology.transformer;




import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TransformerEntityType {
    public TransformerEntityType() {
    }

    public TransformerEntityType(String id,String label,String type, String name, List<Map<String,String>> metadataProperties) {
        this.id = id;
        this.label = label;
        this.eType = type;
        this.pattern = name;
        this.metadataProperties = metadataProperties;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String geteType() {
        return eType;
    }

    public void seteType(String eType) {
        this.eType = eType;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public TransformerProperties getProperties() {
        return properties;
    }

    public void setProperties(TransformerProperties properties) {
        this.properties = properties;
    }

    public List<Map<String,String>> getMetadataProperties() {
        return metadataProperties;
    }

    public void setMetadataProperties(List<Map<String,String>> metadataProperties) {
        this.metadataProperties = metadataProperties;
    }

    public boolean hasMetadataProperty(String key) {
        return this.getMetadataProperties().stream().filter(map -> map.containsKey(key)).findAny().isPresent();
    }

    public Optional<Map<String, String>> metadataProperty(String key) {
        return this.getMetadataProperties().stream().filter(map -> map.containsKey(key)).findAny();
    }

    @Override
    public String toString()
    {
        return "EntityType [id = "+id+",eType = "+eType+", name = "+ pattern +", label = "+ label +", properties = "+ metadataProperties +"]";
    }

    private String label;
    //region Fields
    private String eType;
    private String id;
    private String pattern;
    private List<Map<String,String>> metadataProperties;
    private TransformerProperties properties;

    //endregion


}
