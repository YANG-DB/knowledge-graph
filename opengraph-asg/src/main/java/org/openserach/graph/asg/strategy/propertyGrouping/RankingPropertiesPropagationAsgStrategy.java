package org.openserach.graph.asg.strategy.propertyGrouping;

/*-
 * #%L
 * opengraph-asg
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */







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
