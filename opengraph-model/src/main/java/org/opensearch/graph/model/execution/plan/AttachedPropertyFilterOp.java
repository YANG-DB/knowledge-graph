package org.opensearch.graph.model.execution.plan;




import org.opensearch.graph.model.query.properties.constraint.Constraint;

/**
 * Created by lior.perry on 22/02/2017.
 */
public class AttachedPropertyFilterOp extends PlanOp {
    //region Constructor
    public AttachedPropertyFilterOp() {

    }

    public AttachedPropertyFilterOp(String propName, Constraint condition) {
        this.propName = propName;
        this.condition = condition;
    }
    //endregion

    //region Properties
    public String getPropName() {
        return this.propName;
    }

    public void setPropName(String value) {
        this.propName = value;
    }

    public Constraint getCondition() {
        return this.condition;
    }

    public void setCondition(Constraint value) {
        this.condition = value;
    }
    //endregion

    //region Fields
    private String propName;
    private Constraint condition;
    //endregion
}
