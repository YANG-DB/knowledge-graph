package org.openserach.graph.asg.strategy.constraint;




import org.openserach.graph.asg.strategy.AsgStrategy;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.opensearch.graph.model.query.quant.QuantType;
import javaslang.collection.Stream;

import java.util.List;

import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.ignorableConstraints;

/**
 * search for "like" / "likeAny" constraint within a EpropGroup that has "*" expression in it (in the list of expressions within likeAny)
 * if one found =>
 *                 if the parent EpropGroup is 'All' remove only the found Eprop
 *                 if the parent EpropGroup is 'Some' remove all the other Eprops and EpropGroups in that parent
 */
public class RedundantLikeConstraintAsgStrategy implements AsgStrategy {
    //region ConstraintTransformationAsgStrategyBase Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {

        AsgQueryUtil.elements(query, EPropGroup.class).forEach(ePropGroupAsgEBase -> {
            cleanRedundantLikeEpropsAndGrops(ePropGroupAsgEBase.geteBase());

            if (!getRedundantLikeEprops(ePropGroupAsgEBase.geteBase()).isEmpty() &&
                    ePropGroupAsgEBase.geteBase().getQuantType().equals(QuantType.some) ) {
                ePropGroupAsgEBase.geteBase().getProps().clear();
                ePropGroupAsgEBase.geteBase().getGroups().clear();
            }
        });
    }
    //endregion

    //region Private Methods
    private void cleanRedundantLikeEpropsAndGrops(EPropGroup ePropGroup) {
        List<EProp> redundantEprops = getRedundantLikeEprops(ePropGroup);

        if (!redundantEprops.isEmpty()) {
            if (ePropGroup.getQuantType().equals(QuantType.all)) {
                //remove all non needed '*' ePropGroup
                ePropGroup.getProps().removeAll(redundantEprops);
            } else if (ePropGroup.getQuantType().equals(QuantType.some)) {
                ePropGroup.getProps().removeIf(eprop -> eprop != redundantEprops.get(0));
                ePropGroup.getGroups().clear();
            }
        }

        ePropGroup.getGroups().forEach(this::cleanRedundantLikeEpropsAndGrops);

        List<EPropGroup> redundantGroups = getRedundantGroups(ePropGroup);

        if (!redundantGroups.isEmpty()) {
            if (ePropGroup.getQuantType().equals(QuantType.all)) {
                ePropGroup.getGroups().removeAll(redundantGroups);
            } else if (ePropGroup.getQuantType().equals(QuantType.some)) {
                ePropGroup.getProps().clear();
                ePropGroup.getGroups().clear();
                ePropGroup.getProps().add(getRedundantLikeEprops(redundantGroups.get(0)).get(0));
            }
        }
    }

    private List<EProp> getRedundantLikeEprops(EPropGroup ePropGroup) {
        return Stream.ofAll(ePropGroup.getProps())
                .filter(prop -> prop.getCon() != null)
                .filter(prop -> !ignorableConstraints.contains(prop.getCon().getClass()))
                .filter(prop -> (
                        prop.getCon().getOp().equals(ConstraintOp.like) &&
                                prop.getCon().getExpr().toString().matches("[*]+")) ||
                        prop.getCon().getOp().equals(ConstraintOp.likeAny) &&
                                Stream.ofAll((List) prop.getCon().getExpr())
                                        .filter(value -> value.toString().matches("[*]+")).toJavaOptional().isPresent())
                .toJavaList();
    }

    private List<EPropGroup> getRedundantGroups(EPropGroup ePropGroup) {
        return Stream.ofAll(ePropGroup.getGroups())
                .filter(group -> group.getQuantType().equals(QuantType.some))
                .filter(group -> !getRedundantLikeEprops(group).isEmpty())
                .toJavaList();
    }
    //endregion
}




