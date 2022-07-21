package org.opensearch.graph.epb.plan.estimation.cache;


import org.opensearch.graph.model.descriptors.Descriptor;
import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.query.properties.BaseProp;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.EPropGroup;
import javaslang.collection.Stream;

import java.util.List;

public class EntityFilterOpDescriptor implements Descriptor<EntityFilterOp> {
    //region Descriptor Implementation
    @Override
    public String describe(EntityFilterOp item) {
        StringBuilder sb = new StringBuilder();
        appendEPropGroup(sb, item.getAsgEbase().geteBase());

        return sb.toString();
    }
    //endregion

    //region Private Methods
    private void appendEPropGroup(StringBuilder sb, EPropGroup ePropGroup) {
        List<EProp> eprops = Stream.ofAll(ePropGroup.getProps())
                .filter(eProp -> eProp.getCon() != null)
                .sortBy(BaseProp::getpType).toJavaList();

        for(EProp eProp : eprops) {
            appendEProp(sb, eProp);
        }

        for(EPropGroup childGroup : ePropGroup.getGroups()) {
            sb.append("[");
            appendEPropGroup(sb, childGroup);
            sb.append("]");
        }
    }

    private void appendEProp(StringBuilder sb, EProp eProp) {
        sb.append(eProp.getpType()).append(".")
                .append(eProp.getCon().getOp().toString()).append(".");

        String value = null;
        if (eProp.getCon().getExpr() == null) {
            sb.append("null");
        } else if (List.class.isAssignableFrom(eProp.getCon().getExpr().getClass())) {
            Stream.ofAll((List)eProp.getCon().getExpr()).take(10)
                    .forEach(val -> {
                        if (val == null) {
                            sb.append("null").append(",");
                        } else {
                            sb.append(val).append(",");
                        }
                    });
        } else {
            sb.append(eProp.getCon().getExpr());
        }
        sb.append(";");
    }
    //endregion
}
