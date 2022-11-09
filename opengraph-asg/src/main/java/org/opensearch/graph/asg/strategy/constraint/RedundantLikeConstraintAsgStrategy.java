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
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.opensearch.graph.model.query.quant.QuantType;
import javaslang.collection.Stream;

import java.util.List;

import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.ignorableConstraints;

/**
 * This strategy transforms redundant appearances of a like predicates and unifies it into a single like predicate
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




