package org.openserach.graph.asg.strategy.propertyGrouping;




import org.openserach.graph.asg.strategy.AsgStrategy;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.RankingProp;
import org.opensearch.graph.model.query.properties.ScoreEPropGroup;
import javaslang.collection.Stream;

import java.util.List;

public class RankingPropertiesPropagationAsgStrategy implements AsgStrategy {

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        //go over all EPropGroups and replace with a scored group in case the group is not scored
        // and contains some descendant which is scored
        Stream.ofAll(AsgQueryUtil.elements(query, EPropGroup.class))
                .forEach(property -> {
                    property.seteBase(replaceRecursive(property.geteBase()));
                });
    }

    private EPropGroup replaceRecursive(EPropGroup group){
        if(group  instanceof RankingProp){
            return group;
        }
        List<EPropGroup> newGroups = Stream.ofAll(group.getGroups()).map(g -> replaceRecursive(g)).toJavaList();
        group.getGroups().clear();
        group.getGroups().addAll(newGroups);

        if(!Stream.ofAll(group.getProps()).find(p -> p instanceof RankingProp).isEmpty()
                || !Stream.ofAll(group.getGroups()).find(g -> g instanceof RankingProp).isEmpty() ){
            return new ScoreEPropGroup(group, 1);
        }else{
          return group;
        }
    }

    //endregion
}
