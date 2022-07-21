package org.opensearch.graph.model.query.aggregation;




import org.opensearch.graph.model.Tagged;
import org.opensearch.graph.model.query.properties.constraint.Constraint;

/**
 * Created by lior.perry on 19/02/2017.
 */
public class Agg extends AggLBase implements Tagged {
    public Agg(int eNum, String eTag,Constraint constraint,  int next) {
        super(eNum);
        seteTag(eTag);
        setCon(constraint);
        setNext(next);
    }

    //region Properties
    public String geteTag() {
        return this.eTag;
    }

    public void seteTag(String value) {
        this.eTag = value;
    }
    //endregion

    //region Fields
    private String eTag;
    //endregion
}
