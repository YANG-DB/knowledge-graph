package org.opensearch.graph.epb.plan.estimation.count;

/*-
 * #%L
 * virtual-epb
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
import org.opensearch.graph.dispatcher.epb.CostEstimatorDriver;
import org.opensearch.graph.dispatcher.gta.PlanTraversalTranslator;
import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.epb.plan.estimation.IncrementalEstimationContext;
import org.opensearch.graph.executor.ontology.UniGraphProvider;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.DoubleCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.resourceInfo.GraphError;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.unipop.process.Profiler;

import java.util.Collections;

import static org.unipop.process.Profiler.PROFILER;

public class CountCostEstimator implements CostEstimatorDriver<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>,GraphTraversal<?, ?>> {
    //region Constructors
    @Inject
    public CountCostEstimator(
            OntologyProvider ontologyProvider,
            PlanTraversalTranslator planTraversalTranslator,
            UniGraphProvider uniGraphProvider) {

        this.ontologyProvider = ontologyProvider;
        this.planTraversalTranslator = planTraversalTranslator;
        this.uniGraphProvider = uniGraphProvider;
    }
    //endregion

    //region CostEstimator Implementation
    @Override
    public PlanWithCost<Plan, PlanDetailedCost> estimate(Plan plan, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery> estimationContext) {
        String ont = estimationContext.getQuery().getOnt();
        Ontology ontology = this.ontologyProvider.get(ont)
                .orElseThrow(() -> new GraphError.GraphErrorException(new GraphError("No target Ontology field found ", "No target Ontology found for " + ont)));

        GraphTraversal<?, ?> traversal = null;
        try {
            traversal = this.planTraversalTranslator.translate(
                    new PlanWithCost<>(plan, new PlanDetailedCost(new DoubleCost(0.0), Collections.emptyList())),
                    new TranslationContext(
                            new Ontology.Accessor(ontology),
                            uniGraphProvider.getGraph(ontology).traversal()));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        //todo add configuration activation
        traversal.asAdmin().getSideEffects().register(PROFILER, Profiler.Impl::new, null);

        return new PlanWithCost<>(plan, new PlanDetailedCost(new DoubleCost(count(traversal)), Collections.emptyList()));
    }

    public Long count(GraphTraversal<?, ?> traversal) {
        return traversal.count().next();
    }
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    private PlanTraversalTranslator planTraversalTranslator;
    private UniGraphProvider uniGraphProvider;
    //endregion
}
