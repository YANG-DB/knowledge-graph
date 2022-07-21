package org.opensearch.graph.model.query.aggregation;




/**
 * Created by lior.perry on 19/02/2017.
 */
public abstract class AggMBase extends AggBase {
    public AggMBase(int eNum) {
        super(eNum);
    }

    //region Properties
    public int getN() {
        return this.n;
    }

    public void setN(int value) {
        this.n = value;
    }

    public AggMOp getOp() {
        return this.op;
    }

    public void setOp(AggMOp value) {
        this.op = value;
    }
    //endregion

    //region Fields
    private int n;
    private AggMOp op;
    //endregion
}
