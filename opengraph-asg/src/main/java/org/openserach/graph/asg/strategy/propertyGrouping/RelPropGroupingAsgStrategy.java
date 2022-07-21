package org.openserach.graph.asg.strategy.propertyGrouping;





import org.openserach.graph.asg.strategy.AsgStrategy;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.properties.RelProp;
import org.opensearch.graph.model.query.properties.RelPropGroup;
import org.opensearch.graph.model.query.quant.HQuant;
import javaslang.collection.Stream;

import java.util.List;

public class RelPropGroupingAsgStrategy implements AsgStrategy {
    // Rel with RelProps e.g., Q190, Q10 on V1
    // AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Stream.ofAll(AsgQueryUtil.elements(query, Rel.class))
                .filter(asgEBase -> !AsgQueryUtil.bDescendant(asgEBase, HQuant.class).isPresent())
                .filter(asgEBase -> !AsgQueryUtil.bAdjacentDescendant(asgEBase, RelPropGroup.class).isPresent())
                .forEach(this::groupRelProps);
    }
    //endregion

    //region Private Methods
    private void groupRelProps(AsgEBase<Rel> asgEBase) {
        RelPropGroup rPropGroup;

        List<AsgEBase<RelProp>> relPropsAsgBChildren = AsgQueryUtil.bDescendants(
                asgEBase,
                (asgEBase1) -> asgEBase1.geteBase().getClass().equals(RelProp.class),
                (asgEBase1) -> asgEBase1.geteBase().getClass().equals(RelProp.class) ||
                        asgEBase1.geteBase().getClass().equals(Rel.class));

        List<RelProp> rProps = Stream.ofAll(relPropsAsgBChildren).map(AsgEBase::geteBase).toJavaList();
        if (rProps.size() > 0) {
            rPropGroup = new RelPropGroup(rProps);
            rPropGroup.seteNum(Stream.ofAll(rProps).map(RelProp::geteNum).min().get());
            relPropsAsgBChildren.forEach(asgEBase::removeBChild);
        } else {
            rPropGroup = new RelPropGroup();
            rPropGroup.seteNum(100*asgEBase.geteNum() + 1);
        }
        asgEBase.addBChild(new AsgEBase<>(rPropGroup));
    }
    //endregion
}
