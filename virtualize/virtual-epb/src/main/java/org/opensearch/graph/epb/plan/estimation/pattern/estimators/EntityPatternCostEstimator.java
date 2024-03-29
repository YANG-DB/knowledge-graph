package org.opensearch.graph.epb.plan.estimation.pattern.estimators;

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





import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.epb.plan.estimation.pattern.EntityPattern;
import org.opensearch.graph.epb.plan.estimation.pattern.Pattern;
import org.opensearch.graph.epb.plan.estimation.IncrementalEstimationContext;
import org.opensearch.graph.epb.plan.statistics.StatisticsProvider;
import org.opensearch.graph.epb.plan.statistics.StatisticsProviderFactory;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.*;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.CountEstimatesCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.resourceInfo.GraphError;

public class EntityPatternCostEstimator implements PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> {
    //region Constructors
    public EntityPatternCostEstimator(StatisticsProviderFactory statisticsProviderFactory, OntologyProvider ontologyProvider) {
        this.statisticsProviderFactory = statisticsProviderFactory;
        this.ontologyProvider = ontologyProvider;
    }
    //endregion

    //region StepPatternCostEstimator Implementation
    @Override
    public PatternCostEstimator.Result<Plan, CountEstimatesCost> estimate(
            Pattern pattern,
            IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery> context) {
        if (!EntityPattern.class.isAssignableFrom(pattern.getClass())) {
            return PatternCostEstimator.EmptyResult.get();
        }

        EntityPattern entityStep = (EntityPattern) pattern;
        EntityOp start = entityStep.getStart();
        EntityFilterOp startFilter = entityStep.getStartFilter();

        //todo - verify ontology name was not change during Asg reWrite as part of mapping phase
        StatisticsProvider statisticsProvider = this.statisticsProviderFactory.get(this.ontologyProvider.get(context.getQuery().getOnt())
                .orElseThrow(() -> new GraphError.GraphErrorException(new GraphError("No target Ontology field found ", "No target Ontology found for " + context.getQuery().getOnt()))));

        //estimate
        double entityTotal = statisticsProvider.getNodeStatistics(start.getAsgEbase().geteBase()).getTotal();
        double filterTotal = entityTotal;
        if (startFilter.getAsgEbase() != null) {
            filterTotal = statisticsProvider.getNodeFilterStatistics(start.getAsgEbase().geteBase(), startFilter.getAsgEbase().geteBase()).getTotal();
        }

        double min = Math.ceil(Math.min(entityTotal, filterTotal));
        return PatternCostEstimator.Result.of(new double[]{1.0}, new PlanWithCost<>(new Plan(start, startFilter), new CountEstimatesCost(min, min)));
    }
    //endregion

    //region Fields
    private StatisticsProviderFactory statisticsProviderFactory;
    private OntologyProvider ontologyProvider;
    //endregion
}
