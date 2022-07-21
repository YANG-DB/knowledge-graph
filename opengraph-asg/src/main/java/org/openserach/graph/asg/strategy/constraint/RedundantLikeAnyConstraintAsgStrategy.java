package org.openserach.graph.asg.strategy.constraint;




import org.openserach.graph.asg.strategy.AsgStrategy;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import javaslang.collection.Stream;

import java.util.List;

import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.ignorableConstraints;

/**
 * This strategy replaces a likeAny constraint with a single value to an equivalent like constraint with a single value
 */
public class RedundantLikeAnyConstraintAsgStrategy implements AsgStrategy {
    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        AsgQueryUtil.elements(query, EPropGroup.class).forEach(ePropGroupAsgEBase -> {
            Stream.ofAll(ePropGroupAsgEBase.geteBase().getProps())
                    .filter(eProp -> eProp.getCon() != null)
                    .filter(prop -> !ignorableConstraints.contains(prop.getCon().getClass()))
                    .filter(eProp -> eProp.getCon().getOp().equals(ConstraintOp.likeAny))
                    .filter(eProp -> ((List) eProp.getCon().getExpr()).size() == 1)
                    .forEach(eProp -> eProp.setCon(Constraint.of(ConstraintOp.like, ((List) eProp.getCon().getExpr()).get(0))));
        });
    }
    //endregion
}
