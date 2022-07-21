package org.openserach.graph.asg.strategy.constraint;




import org.openserach.graph.asg.strategy.AsgStrategy;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.query.properties.BaseProp;
import org.opensearch.graph.model.query.properties.BasePropGroup;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.RelPropGroup;
import org.opensearch.graph.model.query.quant.QuantType;
import javaslang.collection.Stream;

public class RedundantPropGroupAsgStrategy implements AsgStrategy {
    //region ConstraintTransformationAsgStrategyBase Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        AsgQueryUtil.elements(query, EPropGroup.class).forEach(ePropGroupAsgEBase -> {
            simplifyPropGroup(ePropGroupAsgEBase.geteBase());
        });

        AsgQueryUtil.elements(query, RelPropGroup.class).forEach(ePropGroupAsgEBase -> {
            simplifyPropGroup(ePropGroupAsgEBase.geteBase());
        });
    }
    //endregion

    //region Private Methods
    private <S extends BaseProp, T extends BasePropGroup<S, T>> void simplifyPropGroup(BasePropGroup<S, T> propGroup) {
        Stream.ofAll(propGroup.getGroups()).forEach(this::simplifyPropGroup);

        if (isSimplePropGroup(propGroup)) {
            propGroup.setQuantType(QuantType.all);

            if (!propGroup.getGroups().isEmpty()) {
                T childGroup = propGroup.getGroups().get(0);
                /*if (isSimplePropGroup(childGroup)) {
                    propGroup.getGroups().remove(childGroup);
                    propGroup.getGroups().addAll(childGroup.getGroups());
                    propGroup.getProps().addAll(childGroup.getProps());
                }*/

                propGroup.getGroups().remove(childGroup);

                propGroup.getGroups().addAll(childGroup.getGroups());
                propGroup.getProps().addAll(childGroup.getProps());
                propGroup.setQuantType(childGroup.getQuantType());
            }
        }
    }

    private <S extends BaseProp, T extends BasePropGroup<S, T>> boolean isSimplePropGroup(BasePropGroup<S, T> propGroup) {
        return propGroup.getGroups().size() + propGroup.getProps().size() <= 1;
    }
    //endregion
}
