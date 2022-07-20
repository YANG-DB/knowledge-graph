package org.opensearch.graph.gta.strategy.promise;

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
import org.opensearch.graph.gta.strategy.common.CompositePlanOpTranslationStrategy;
import org.opensearch.graph.gta.strategy.common.EntityTranslationOptions;
import org.opensearch.graph.gta.strategy.common.GoToEntityOpTranslationStrategy;
import org.opensearch.graph.gta.strategy.common.JoinEntityOpTranslationStrategy;
import org.opensearch.graph.gta.translation.ChainedPlanOpTraversalTranslator;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

/**
 * Created by Roman on 11/05/2017.
 */
public class M2PlanOpTranslationStrategy extends CompositePlanOpTranslationStrategy {
    //region Constructors
    public M2PlanOpTranslationStrategy() {
        super(
                new EntityOpTranslationStrategy(EntityTranslationOptions.none),
                new GoToEntityOpTranslationStrategy(),
                new RelationOpTranslationStrategy(),
                new CompositePlanOpTranslationStrategy(
                        new EntityFilterOpTranslationStrategy(EntityTranslationOptions.none),
                        new EntitySelectionTranslationStrategy()),
                new RelationFilterOpTranslationStrategy());

        this.strategies = Stream.ofAll(this.strategies)
                .append(new JoinEntityOpTranslationStrategy(new ChainedPlanOpTraversalTranslator(this), EntityJoinOp.class));
    }
    //endregion


    @Override
    public GraphTraversal translate(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOp planOp, TranslationContext context) {
        return super.translate(traversal, plan, planOp, context);
    }
}
