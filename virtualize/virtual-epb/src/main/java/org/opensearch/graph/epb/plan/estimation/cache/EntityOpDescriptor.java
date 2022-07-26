package org.opensearch.graph.epb.plan.estimation.cache;





import org.opensearch.graph.model.descriptors.Descriptor;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.query.entity.EConcrete;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.entity.EUntyped;

public class EntityOpDescriptor implements Descriptor<EntityOp> {
    @Override
    public String describe(EntityOp item) {
        return ETyped.class.isAssignableFrom(item.getAsgEbase().geteBase().getClass()) ?
                EConcrete.class.isAssignableFrom(item.getAsgEbase().geteBase().getClass()) ?
                        ((EConcrete)item.getAsgEbase().geteBase()).geteType() + "(" + ((EConcrete)item.getAsgEbase().geteBase()).geteID() + ")" :
                        ((ETyped)item.getAsgEbase().geteBase()).geteType() :
                EUntyped.class.getSimpleName();
    }
}
