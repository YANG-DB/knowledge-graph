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
