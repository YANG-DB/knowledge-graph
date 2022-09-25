package org.opensearch.graph.asg.strategy.constraint;

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







import org.opensearch.graph.asg.strategy.AsgStrategy;
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
