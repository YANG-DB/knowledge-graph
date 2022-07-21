package org.opensearch.graph.stats.model.configuration;


public class Filter {

    //region Ctrs
    public Filter() {
        //needed for Jackson
    }

    public Filter(String name, String value) {
        this.name = name;
        this.value = value;
    }
    //endregion

    //region Getters & Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    //endregion

    //region Fields
    private String name;
    private String value;
    //endregion
}
