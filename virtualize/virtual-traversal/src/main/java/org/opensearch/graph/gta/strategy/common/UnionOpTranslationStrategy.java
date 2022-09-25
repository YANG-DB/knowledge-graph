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





import org.opensearch.graph.dispatcher.gta.PlanTraversalTranslator;
import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.gta.strategy.PlanOpTranslationStrategy;
import org.opensearch.graph.gta.strategy.PlanOpTranslationStrategyBase;
import org.opensearch.graph.gta.translation.ChainedPlanOpTraversalTranslator;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.composite.UnionOp;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.unipop.process.traversal.dsl.graph.SearchGraphTraversalSource;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.List;

public class UnionOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    private final PlanOpTranslationStrategy planOpTranslationStrategy;

    public UnionOpTranslationStrategy(PlanOpTranslationStrategy planOpTranslationStrategy) {
        super(planOp -> planOp.getClass().equals(UnionOp.class));
        this.planOpTranslationStrategy = planOpTranslationStrategy;
    }

    @Override
    protected GraphTraversal translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> planWithCost, PlanOp planOp, TranslationContext context) {
        UnionOp unionOp = (UnionOp)planOp;

        int indexOfUnion = planWithCost.getPlan().getOps().indexOf(planOp);
        final List<? extends GraphTraversal<?, ?>> traversalList = Stream.ofAll(unionOp.getPlans()).map(plan -> {
            //create a (one of) branch union plan
            Plan unionPlan = new Plan(Stream.ofAll(planWithCost.getPlan().getOps()).take(indexOfUnion).toJavaList()).append(plan);
            //create chained plan translator
            PlanTraversalTranslator planTraversalTranslator = new ChainedPlanOpTraversalTranslator(this.planOpTranslationStrategy, indexOfUnion);
            //return the gremlin translated plan
            return planTraversalTranslator.translate(new PlanWithCost<>(unionPlan, planWithCost.getCost()), context);
        }).toJavaList();
        //traversal union translated branches
        return ((SearchGraphTraversalSource) context.getGraphTraversalSource()).union(traversalList.toArray(new GraphTraversal[traversalList.size()]));
    }
}
