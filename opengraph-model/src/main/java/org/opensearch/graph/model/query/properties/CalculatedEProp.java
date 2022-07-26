package org.opensearch.graph.model.query.properties;






import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.query.properties.projection.CalculatedFieldProjection;

/**
 *
 * a calculated field based on tag associated with the entity or entity relation
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CalculatedEProp extends EProp {
    //region Constructors
    public CalculatedEProp() {

    }

    public CalculatedEProp(int eNum, String expression, CalculatedFieldProjection con) {
        super(eNum, expression, con);
    }
    //endregion

    //region Override Methods
    //endregion

    @Override
    public CalculatedEProp clone() {
        return clone(geteNum());
    }

    @Override
    public CalculatedEProp clone(int eNum) {
        CalculatedEProp clone = new CalculatedEProp();
        clone.seteNum(eNum);
        clone.setF(getF());
        clone.setProj(getProj());
        clone.setCon(getCon());
        clone.setpTag(getpTag());
        clone.setpType(getpType());
        return clone;
    }

    public static CalculatedEProp of(int eNum, String expression, CalculatedFieldProjection con) {
        return new CalculatedEProp(eNum, expression, con);
    }

    @Override
    public CalculatedFieldProjection getProj() {
        return (CalculatedFieldProjection) super.getProj();
    }
}
