package org.openserach.graph.asg.strategy.constraint;





import org.openserach.graph.asg.strategy.AsgStrategy;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import javaslang.collection.Stream;

import java.util.Arrays;
import java.util.List;

import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.ignorableConstraints;

public class RedundantInRangeConstraintAsgStrategy implements AsgStrategy {
    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        AsgQueryUtil.elements(query, EPropGroup.class).forEach(ePropGroupAsgEBase -> {
            cleanRedundantInSetEprops(ePropGroupAsgEBase.geteBase());
        });
    }
    //endregion

    //region Private Methods
    private void cleanRedundantInSetEprops(EPropGroup ePropGroup) {
        //todo here we simple take the first and last elements in the expressions list
        //todo we should calculate the real (value & type depended) boundries and use them as [min,max]
        Stream.ofAll(ePropGroup.getProps())
                .filter(eProp -> eProp.getCon() != null)
                .filter(prop -> !ignorableConstraints.contains(prop.getCon().getClass()))
                .filter(eProp -> eProp.getCon().getOp().equals(ConstraintOp.inRange))
                .filter(eProp -> ((List) eProp.getCon().getExpr()).size() > 2)
                .forEach(eProp -> eProp.setCon(Constraint.of(ConstraintOp.inRange,
                        Arrays.asList(((List) eProp.getCon().getExpr()).get(0),
                                ((List) eProp.getCon().getExpr()).get(((List) eProp.getCon().getExpr()).size()-1)))));

        Stream.ofAll(ePropGroup.getGroups()).forEach(this::cleanRedundantInSetEprops);
    }
    //endregion
}
