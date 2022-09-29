package org.opensearch.graph.model.schema;

/*-
 * #%L
 * opengraph-model
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





import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "partition",
        "props",
        "nested"
})
public class Entity implements BaseTypeElement<Entity> {

    @JsonProperty("type")
    private Type type;
    @JsonProperty("partition")
    private String partition;
    @JsonProperty("mapping")
    private String mapping;
    @JsonProperty("props")
    private Props props;
    @JsonProperty("nested")
    private List<Entity> nested = Collections.EMPTY_LIST;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    public Entity() {}

    public Entity(Type type, String partition, String mapping, Props props, List<Entity> nested, Map<String, Object> additionalProperties) {
        this.type = type;
        this.partition = partition;
        this.mapping = mapping;
        this.props = props;
        this.nested = nested;
        this.additionalProperties = additionalProperties;
    }

    @JsonProperty("nested")
    public List<Entity> getNested() {
        return nested;
    }

    @JsonProperty("nested")
    public void setNested(List<Entity> nested) {
        this.nested = nested;
    }

    @JsonProperty("type")
    public Type getType() {
        return type;
    }

    @Override
    public boolean hasProperties() {
        return props != null && !props.getValues().isEmpty();
    }

    @JsonProperty("type")
    public void setType(Type type) {
        this.type = type;
    }

    @JsonProperty("partition")
    public String getPartition() {
        return partition;
    }

    @JsonProperty("mapping")
    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    @JsonProperty("mapping")
    public String getMapping() {
        return mapping;
    }

    @JsonProperty("partition")
    public void setPartition(String partition) {
        this.partition = partition;
    }

    @JsonProperty("props")
    public Props getProps() {
        return props;
    }

    @JsonProperty("props")
    public void setProps(Props props) {
        this.props = props;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @JsonIgnore
    public Entity withMapping(String mapping) {
        this.mapping = mapping;
        return this;
    }

    @Override
    protected Entity clone()  {
        return new Entity(this.type,this.partition,this.mapping,this.props.clone(),
                this.nested.stream().map(Entity::clone).collect(Collectors.toList()),
                new HashMap<>(this.additionalProperties));
    }

    @JsonIgnore
    public Entity withType(Type type) {
        this.type = type;
        return this;
    }

    @JsonIgnore
    public Entity withPartition(String partition) {
        this.partition = partition;
        return this;
    }

}
