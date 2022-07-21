package org.opensearch.graph.model.execution.plan.composite.descriptors;




import org.opensearch.graph.model.descriptors.Descriptor;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;

/**
 * Created by Roman on 3/13/2018.
 */
public class RelationOpDescriptor implements Descriptor<RelationOp> {
    //region Descriptor Implementation
    @Override
    public String describe(RelationOp item) {
        return String.format("%s(%s(%s))",
                item.getClass().getSimpleName(),
                item.getAsgEbase().geteBase().getrType(),
                item.getAsgEbase().geteBase().geteNum());
    }
    //endregion
}
