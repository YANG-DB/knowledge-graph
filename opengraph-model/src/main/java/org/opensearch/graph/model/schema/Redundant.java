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





import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "redundant_name",
        "name",
        "type",
        "side"
})
public class Redundant {

    @JsonProperty("redundant_name")
    private String redundantName;
    @JsonProperty("name")
    private String name;
    @JsonProperty("type")
    private String type;
    @JsonProperty("side")
    private List<String> side = new ArrayList<>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Redundant() {}

    public Redundant(String redundantName, String name, String type, List<String> side, Map<String, Object> additionalProperties) {
        this.redundantName = redundantName;
        this.name = name;
        this.type = type;
        this.side = side;
        this.additionalProperties = additionalProperties;
    }

    @JsonProperty("side")
    public List<String> getSide() {
        return side;
    }

    @JsonProperty("side")
    public void setSide(List<String> side) {
        this.side = side;
    }

    @JsonProperty("redundant_name")
    public String getRedundantName() {
        return redundantName;
    }

    @JsonProperty("redundant_name")
    public void setRedundantName(String redundantName) {
        this.redundantName = redundantName;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    protected Redundant clone()  {
        return new Redundant(this.redundantName,this.name,this.type,new ArrayList<>(this.side),new HashMap<>(this.additionalProperties));
    }
}
