package org.opensearch.graph.model.execution.plan.composite.descriptors;




import org.opensearch.graph.model.descriptors.Descriptor;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.query.entity.ETyped;

/**
 * Created by Roman on 3/13/2018.
 */
public class EntityOpDescriptor implements Descriptor<EntityOp> {
    //region Descriptor Implementation
    @Override
    public String describe(EntityOp item) {
        return ETyped.class.isAssignableFrom(item.getAsgEbase().geteBase().getClass()) ?
            String.format("%s(%s(%s))",
                    item.getClass().getSimpleName(),
                    ((ETyped)item.getAsgEbase().geteBase()).geteType(),
                    item.getAsgEbase().geteBase().geteNum()) :
                String.format("%s(%s)",
                        item.getClass().getSimpleName(),
                        item.getAsgEbase().geteBase().geteNum());
    }
    //endregion
}
