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

/**
 * This strategy simplifies property groups by building equivalent less complex, less nested groups.
 * The strategy searches recursively for 'simple' groups, where a 'simple' group is defined to be a group that has either
 * a single property or a single child group. Any 'simple' group is changed by setting it's quant type to be 'All'
 * (because 'All' and 'Some' are equivalent in a group where there's only 1 property or child group, and 'All' is preferable
 * to 'Some' because currently 'Some' quants will cost more in other aspects of the fuse engine.
 *
 * Further more, if a group is simple and it has a child group, the child groups props and groups are added to the current group
 * the groups quant type is set to be the child groups quant type and the child group itself is discarded.
 * This phase makes nested group structures less complex when they are redundant.
 */
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
