package org.opensearch.graph.model.query.aggregation;




import org.opensearch.graph.model.query.EBase;

/**
 * Created by lior.perry on 22/02/2017.
 */
public class AggBase extends EBase {
    public AggBase(int eNum) {
        super(eNum);
    }

    //region Properties
    public String[] getPer() {
        return this.per;
    }

    public void setPer(String[] value) {
        this.per = value;
    }

    public int getB() {
        return this.b;
    }

    public void setB(int value) {
        this.b = value;
    }

    public int getNext() {
        return next;
    }

    public void setNext(int next) {
        this.next = next;
    }

    //endregion

    //region Fields
    private String[] per;
    private int next = -1;
    private int b;
    //endregion
}
