package org.opensearch.graph.stats.model.result;


import org.opensearch.graph.stats.model.enums.DataType;

public class StatGlobalCardinalityResult extends StatResultBase{

    //region Ctors
    public StatGlobalCardinalityResult() {
        super();
    }

    public StatGlobalCardinalityResult(String index, String type, String field, String direction, long count, long cardinality) {
        super(index, type, field, field + "_" + direction, DataType.string, count, cardinality);
        this.direction = direction;
    }

    //endregion

    //region Getter & Setters
    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
    //endregion

    //region Fields
    private String direction;
    //endregion
}
