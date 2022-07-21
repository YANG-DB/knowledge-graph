package org.opensearch.graph.stats.model.configuration;



import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Index {

    //region Getters & Setters
    @JsonProperty("index")
    public String getIndex() {
        return index;
    }

    @JsonProperty("index")
    public void setIndex(String index) {
        this.index = index;
    }

    @JsonProperty("types")
    public List<Type> getTypes() {
        return types;
    }

    @JsonProperty("types")
    public void setTypes(List<Type> types) {
        this.types = types;
    }
    //endregion

    //region Fields
    private String index;
    private List<Type> types;
    //endregion

}
