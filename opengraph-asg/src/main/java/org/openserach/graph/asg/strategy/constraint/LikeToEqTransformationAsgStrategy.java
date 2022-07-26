package org.openserach.graph.asg.strategy.constraint;







import org.openserach.graph.asg.strategy.AsgStrategy;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import javaslang.collection.Stream;

public class LikeToEqTransformationAsgStrategy implements AsgStrategy {
    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {

        AsgQueryUtil.elements(query, EPropGroup.class).forEach(ePropGroupAsgEBase -> {
            transformGroup(ePropGroupAsgEBase.geteBase());
        });
    }
    //endregion

    //region Private Methods
    private void transformGroup(EPropGroup ePropGroup){
        Stream.ofAll(ePropGroup.getProps())
                .filter(prop -> prop.getCon()!=null)
                .filter(prop -> prop.getCon().getOp().equals(ConstraintOp.like) &&
                !prop.getCon().getExpr().toString().contains("*")).forEach(eProp -> eProp.getCon().setOp(ConstraintOp.eq));

        Stream.ofAll(ePropGroup.getGroups()).forEach(this::transformGroup);
    }
    //endregion
}




