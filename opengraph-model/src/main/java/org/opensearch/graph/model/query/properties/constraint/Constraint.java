package org.opensearch.graph.model.query.properties.constraint;






import com.fasterxml.jackson.annotation.*;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "InnerQueryConstraint", value = InnerQueryConstraint.class),
        @JsonSubTypes.Type(name = "WhereByConstraint", value = WhereByConstraint.class),
        @JsonSubTypes.Type(name = "ParameterizedConstraint", value = ParameterizedConstraint.class)})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Constraint {

    //region Ctrs
    public Constraint() {
    }

    public Constraint(ConstraintOp op) {
        this();
        this.op = op;
    }
    public Constraint(ConstraintOp op, Object expr) {
        this(op);
        this.expr = expr;
    }

    public Constraint(ConstraintOp op, Object expr, String iType) {
        this(op,expr);
        this.iType = iType;
    }
    public Constraint(ConstraintOp op,CountConstraintOp countOp, Object expr, String iType) {
        this(op,expr);
        this.countOp = countOp;
        this.iType = iType;
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        Constraint other = (Constraint) o;

        if (this.op == null) {
            if (other.op != null) {
                return false;
            }
        } else {
            if (!this.op.equals(other.op)) {
                return false;
            }
        }

        if (this.expr == null) {
            if (other.expr != null) {
                return false;
            }
        } else {
            if (!this.expr.equals(other.expr)) {
                return false;
            }
        }

        if (this.iType == null) {
            if (other.iType != null) {
                return false;
            }
        } else {
            if (!this.iType.equals(other.iType)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Constraint clone() {
        return new Constraint(op,countOp,expr,iType);
    }

    //endregion

    //region Properties
    public ConstraintOp getOp() {
        return op;
    }

    public CountConstraintOp getCountOp() {
        return countOp;
    }

    public void setCountOp(CountConstraintOp countOp) {
        this.countOp = countOp;
    }

    public void setOp(ConstraintOp op) {
        this.op = op;
    }

    public Object getExpr() {
        return expr;
    }

    public void setExpr(Object expr) {
        this.expr = expr;
    }

    public String getiType() {
        return iType;
    }

    public void setiType(String iType) {
        this.iType = iType;
    }

    @JsonIgnore
    public boolean isAggregation() {
        return getCountOp()!=null;
    }
    //endregion

    //region Fields
    private ConstraintOp op;
    private CountConstraintOp countOp;
    private Object expr;
    //default - inclusive
    private String iType = "[]";
    //endregion

    public static Constraint of(CountConstraintOp op, Object expr) {
        return new Constraint(null,op,expr,"[]");
    }

    public static Constraint of(ConstraintOp op) {
        return of(op, null, "[]");
    }

    public static Constraint of(ConstraintOp op, Object exp) {
        return of(op, exp, "[]");
    }

    public static Constraint of(ConstraintOp op, Object exp, String iType) {
        Constraint constraint = new Constraint();
        constraint.setExpr(exp);
        constraint.setOp(op);
        constraint.setiType(iType);
        return constraint;
    }

}
