package org.opensearch.graph.stats.model.result;


import org.opensearch.graph.stats.model.enums.DataType;

/**
 * Created by benishue on 24/05/2017.
 */
public class StatTermResult <T> extends StatResultBase{

    //region Ctors
    public StatTermResult() {
        super();
    }

    public StatTermResult(String index,
                          String type,
                          String field,
                          String key,
                          DataType dataType,
                          T term,
                          long count,
                          long cardinality) {
        super(index, type, field, key, dataType, count, cardinality);
        this.term = term;
    }

    //endregion

    //region Getter & Setters
    public T getTerm() {
        return term;
    }

    public void setTerm(T term) {
        this.term = term;
    }
    //endregion

    //region Fields
    private T term;
    //endregion
}
