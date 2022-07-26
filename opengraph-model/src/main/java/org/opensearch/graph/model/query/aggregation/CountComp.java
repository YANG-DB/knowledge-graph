package org.opensearch.graph.model.query.aggregation;






import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.query.EBase;

public class CountComp extends EBase {
    //region Constructors
    public CountComp() {}

    public CountComp(int eNum, int next) {
        super(eNum);
        this.next = next;
    }
    //endregion

    @Override
    public CountComp clone() {
        return clone(geteNum());
    }

    @Override
    public CountComp clone(int eNum) {
        CountComp clone = new CountComp();
        clone.seteNum(eNum);
        return clone;
    }

    //region Properties
    public int getNext() {
        return next;
    }

    public void setNext(int next) {
        this.next = next;
    }
    //endregion

    //region Fields
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int next;
    //endregion
}
