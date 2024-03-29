package org.opensearch.graph.gta.strategy.discrete;

/*-
 * #%L
 * virtual-traversal
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





import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.dispatcher.utils.PlanUtil;
import org.opensearch.graph.gta.strategy.PlanOpTranslationStrategyBase;
import org.opensearch.graph.gta.strategy.utils.ConversionUtil;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.query.properties.BasePropGroup;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.constraint.WhereByConstraint;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.*;

import static java.util.stream.Stream.*;

public class WhereByOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    //region Constructors
    public WhereByOpTranslationStrategy() {
        super(planOp -> planOp.getClass().equals(EntityFilterOp.class));
    }
    //endregion

    //region PlanOpTranslationStrategyBase Implementation
    @Override
    protected GraphTraversal<?, ?> translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> planWithCost, PlanOp planOp, TranslationContext context) {
        EntityFilterOp filterOp = (EntityFilterOp) planOp;
        EntityOp entityOp = PlanUtil.prev(planWithCost.getPlan(), planOp, EntityOp.class).get();
        String entityTag = entityOp.getAsgEbase().geteBase().geteTag();
        EPropGroup group = filterOp.getAsgEbase().geteBase();

        if (!group.getProps().isEmpty() || !group.getGroups().isEmpty()) {
            List<EProp> props = group.getProps();
            List<EProp> groupProps = Stream.ofAll(group.getGroups()).flatMap(BasePropGroup::getProps).toJavaList();
            Optional<EProp> whereClause = of(props, groupProps)
                    .flatMap(Collection::stream)
                    .filter(this::isWhereClause)
                    .findAny();

            if (whereClause.isPresent()) {
                WhereByConstraint constraint = (WhereByConstraint) whereClause.get().getCon();
                traversal.asAdmin().where(entityTag, ConversionUtil.convertConstraint(constraint))
                        .by(constraint.getProjectedField());
            }
        }


        return traversal;
    }

    private boolean isWhereClause(EProp p) {
        return p.getCon() != null && p.getCon() instanceof WhereByConstraint;
    }
    //endregion

}
