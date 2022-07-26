package org.opensearch.graph.model.query.optional;






import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.Container;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.quant.QuantType;

/**
 * Created by roman.margolis on 20/11/2017.
 */
public class OptionalComp extends EBase implements Container<Integer> {
    //region Constructors
    public OptionalComp() {

    }

    public OptionalComp(int eNum, int next) {
        super(eNum);
        this.next = next;
    }
    //endregion

    @Override
    public OptionalComp clone() {
        return clone(geteNum());
    }

    @Override
    public OptionalComp clone(int eNum) {
        OptionalComp clone = new OptionalComp();
        clone.seteNum(eNum);
        return clone;
    }

    //region Properties
    public Integer getNext() {
        return next;
    }

    @Override
    public boolean hasNext() {
        return this.next!=0;
    }

    public void setNext(Integer next) {
        this.next = next;
    }
    //endregion

    //region Fields
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int next;

    @Override
    public QuantType getqType() {
        return QuantType.some;
    }
    //endregion
}
