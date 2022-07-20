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
import org.opensearch.graph.gta.strategy.PlanOpTranslationStrategyBase;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.CountEstimatesCost;
import org.opensearch.graph.model.execution.plan.costs.JoinCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import org.opensearch.graph.unipop.process.JoinStep;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.DefaultGraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.function.Predicate;

public class JoinEntityOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    PlanTraversalTranslator planTraversalTranslator;

    @SafeVarargs
    public JoinEntityOpTranslationStrategy(PlanTraversalTranslator planTraversalTranslator, Class<? extends PlanOp>... klasses) {
        super(klasses);
        this.planTraversalTranslator = planTraversalTranslator;
    }

    public JoinEntityOpTranslationStrategy(Predicate<PlanOp> planOpPredicate, PlanTraversalTranslator planTraversalTranslator) {
        super(planOpPredicate);
        this.planTraversalTranslator = planTraversalTranslator;
    }

    @Override
    protected GraphTraversal translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOp planOp, TranslationContext context) {
        if(planOp instanceof EntityJoinOp){
            JoinCost joinCost = (JoinCost) plan.getCost().getPlanStepCost(planOp).get().getCost();
            CountEstimatesCost leftCost = Stream.ofAll(joinCost.getLeftBranchCost().getPlanStepCosts()).last().getCost();
            CountEstimatesCost rightCost = Stream.ofAll(joinCost.getRightBranchCost().getPlanStepCosts()).last().getCost();

            traversal = new DefaultGraphTraversal(context.getGraphTraversalSource());
            traversal.asAdmin().addStep(new JoinStep(traversal.asAdmin()));

            if(leftCost.peek() < rightCost.peek()){
                traversal.by(planTraversalTranslator.translate(new PlanWithCost<>(((EntityJoinOp) planOp).getRightBranch(), joinCost.getRightBranchCost()), context));
                traversal.by(planTraversalTranslator.translate(new PlanWithCost<>(((EntityJoinOp) planOp).getLeftBranch(), joinCost.getLeftBranchCost()), context));

            }else{
                traversal.by(planTraversalTranslator.translate(new PlanWithCost<>(((EntityJoinOp) planOp).getLeftBranch(), joinCost.getLeftBranchCost()), context));
                traversal.by(planTraversalTranslator.translate(new PlanWithCost<>(((EntityJoinOp) planOp).getRightBranch(), joinCost.getRightBranchCost()), context));
            }
        }
        return traversal;
    }
}
