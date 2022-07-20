package org.opensearch.graph.gta.strategy.common;

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

import org.opensearch.graph.gta.strategy.PlanOpTranslationStrategy;
import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

/**
 * Created by Roman on 10/05/2017.
 */
public class CompositePlanOpTranslationStrategy implements PlanOpTranslationStrategy {
    //region Constructors
    public CompositePlanOpTranslationStrategy(PlanOpTranslationStrategy...strategies) {
        this.strategies = Stream.of(strategies).toJavaList();
    }

    public CompositePlanOpTranslationStrategy(Iterable<PlanOpTranslationStrategy> strategies) {
        this.strategies = Stream.ofAll(strategies).toJavaList();
    }
    //endregion

    //region PlanOpTranslationStrategy Implementation
    @Override
    public GraphTraversal translate(GraphTraversal traversal, PlanWithCost<Plan,PlanDetailedCost> plan, PlanOp planOp, TranslationContext context) {
        for(PlanOpTranslationStrategy planOpTranslationStrategy : this.strategies) {
            traversal = planOpTranslationStrategy.translate(traversal, plan, planOp, context);
        }

        return traversal;
    }
    //endregion

    //region Fields
    protected Iterable<PlanOpTranslationStrategy> strategies;
    //endregion
}
