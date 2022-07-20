package org.opensearch.graph.gta.translation;

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

import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.gta.PlanTraversalTranslator;
import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.gta.strategy.*;
import org.opensearch.graph.model.execution.plan.*;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.unipop.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.opensearch.graph.gta.strategy.PlanOpTranslationStrategy;

/**
 * Created by moti on 3/7/2017.
 */
public class ChainedPlanOpTraversalTranslator implements PlanTraversalTranslator {
    //region Constructors
    @Inject
    public ChainedPlanOpTraversalTranslator(PlanOpTranslationStrategy translationStrategy) {
        this.translationStrategy = translationStrategy;
        this.startFrom = 0;
    }

    public ChainedPlanOpTraversalTranslator(PlanOpTranslationStrategy translationStrategy, int startFrom) {
        this.translationStrategy = translationStrategy;
        this.startFrom = startFrom;
    }
    //endregion

    //region PlanTraversalTranslator Implementation
    public GraphTraversal<?, ?> translate(PlanWithCost<Plan, PlanDetailedCost> planWithCost, TranslationContext context) {
        GraphTraversal traversal = __.start();
        for (int planOpIndex = this.startFrom; planOpIndex < planWithCost.getPlan().getOps().size(); planOpIndex++) {
            traversal = this.translationStrategy.translate(traversal, planWithCost, planWithCost.getPlan().getOps().get(planOpIndex), context);
        }

        return traversal;
    }
    //endregion

    //region Fields
    private PlanOpTranslationStrategy translationStrategy;
    private int startFrom;
    //endregion
}
