package org.openserach.graph.asg.strategy.propertyGrouping;




import org.openserach.graph.asg.strategy.AsgStrategy;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.query.properties.RelProp;
import org.opensearch.graph.model.query.properties.RelPropGroup;
import org.opensearch.graph.model.query.quant.HQuant;
import javaslang.collection.Stream;

import java.util.List;

public class HQuantPropertiesGroupingAsgStrategy implements AsgStrategy {
    // Horizontal Quantifier with Bs below
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        AsgQueryUtil.elements(query, HQuant.class).forEach(hQuant -> {
            List<AsgEBase<RelProp>> relPropsAsgBChildren = AsgQueryUtil.bAdjacentDescendants(hQuant, RelProp.class);

            RelPropGroup rPropGroup;
            List<RelProp> relProps = Stream.ofAll(relPropsAsgBChildren).map(AsgEBase::geteBase).toJavaList();

            if (relProps.size() > 0) {
                rPropGroup = new RelPropGroup(relProps);
                rPropGroup.seteNum(Stream.ofAll(relProps).map(RelProp::geteNum).min().get());

                relPropsAsgBChildren.forEach(hQuant::removeBChild);
            } else {
                rPropGroup = new RelPropGroup();
                rPropGroup.seteNum(Stream.ofAll(AsgQueryUtil.eNums(query)).max().get() + 1);
            }

            hQuant.addBChild(new AsgEBase<>(rPropGroup));
        });
    }
    //endregion
}
