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
        "symmetric",
        "props",
        "nested",
        "redundant"
})
public class Relation implements BaseTypeElement<Relation> {

    @JsonProperty("type")
    private String type;
    @JsonProperty("partition")
    private String partition;
    @JsonProperty("mapping")
    private String mapping;
    @JsonProperty("symmetric")
    private boolean symmetric = false;
    @JsonProperty("nested")
    private List<Relation> nested = Collections.EMPTY_LIST;
    @JsonProperty("props")
    private Props props;
    @JsonProperty("redundant")
    private List<Redundant> redundant = Collections.EMPTY_LIST;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Relation() {}

    public Relation(String type, String partition, String mapping, boolean symmetric, List<Relation> nested, Props props, List<Redundant> redundant, Map<String, Object> additionalProperties) {
        this.type = type;
        this.partition = partition;
        this.mapping = mapping;
        this.symmetric = symmetric;
        this.nested = nested;
        this.props = props;
        this.redundant = redundant;
        this.additionalProperties = additionalProperties;
    }

    @JsonProperty("symmetric")
    public boolean isSymmetric() {
        return symmetric;
    }

    @JsonProperty("symmetric")
    public void setSymmetric(boolean symmetric) {
        this.symmetric = symmetric;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("partition")
    public String getPartition() {
        return partition;
    }

    @JsonProperty("partition")
    public void setPartition(String partition) {
        this.partition = partition;
    }


    @JsonProperty("mapping")
    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    @JsonProperty("mapping")
    public String getMapping() {
        return mapping;
    }

    @JsonProperty("props")
    public Props getProps() {
        return props;
    }

    @JsonProperty("props")
    public void setProps(Props props) {
        this.props = props;
    }

    @JsonProperty("nested")
    public List<Relation> getNested() {
        return nested;
    }

    @JsonProperty("nested")
    public void setNested(List<Relation> nested) {
        this.nested = nested;
    }


    @JsonProperty("redundant")
    public List<Redundant> getRedundant() {
        return redundant;
    }

    @JsonProperty("redundant")
    public void setRedundant(List<Redundant> redundant) {
        this.redundant = redundant;
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
    public List<Redundant> getRedundant(String side) {
        return getRedundant().stream().filter(r->r.getSide().contains(side)).collect(Collectors.toList());
    }

    @JsonIgnore
    public Relation withMapping(String mapping) {
        this.mapping = mapping;
        return this;
    }

    @JsonIgnore
    public Relation withType(String type) {
        this.type = type;
        return this;
    }

    @JsonIgnore
    public Relation withPartition(String partition) {
        this.partition = partition;
        return this;
    }

    @Override
    protected Relation clone()  {
        return new Relation(this.type,this.partition,this.mapping,this.symmetric,
                this.nested.stream().map(Relation::clone).collect(Collectors.toList()),
                this.props.clone(),this.redundant.stream().map(Redundant::clone).collect(Collectors.toList()),
                new HashMap<>(this.additionalProperties));
    }

}
