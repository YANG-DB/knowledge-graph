package org.opensearch.graph.epb.plan.estimation.pattern.estimators.rule;





import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.epb.plan.estimation.CostEstimationConfig;
import org.opensearch.graph.epb.plan.estimation.IncrementalEstimationContext;
import org.opensearch.graph.epb.plan.estimation.pattern.EntityPattern;
import org.opensearch.graph.epb.plan.estimation.pattern.EntityRelationEntityPattern;
import org.opensearch.graph.epb.plan.estimation.pattern.Pattern;
import org.opensearch.graph.epb.plan.estimation.pattern.estimators.*;
import org.opensearch.graph.epb.plan.statistics.StatisticsProviderFactory;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.CountEstimatesCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;

import java.util.HashMap;
import java.util.Map;

public class RulesBasedPatternCostEstimator extends CompositePatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> {
    //region Static
    private static Map<Class<? extends Pattern>,
            PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>> estimators(
                    CostEstimationConfig config,
                    StatisticsProviderFactory statisticsProviderFactory,
                    OntologyProvider ontologyProvider) {
        Map<Class<? extends Pattern>, PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>>> estimators =
                new HashMap<>();

        estimators.put(EntityPattern.class, new EntityPatternCostEstimator(statisticsProviderFactory, ontologyProvider));
        estimators.put(EntityRelationEntityPattern.class, new EntityRelationEntityPatternCostEstimator(config, statisticsProviderFactory, ontologyProvider));

        return estimators;
    }
    //endregion

    //region Constructors
    @Inject
    public RulesBasedPatternCostEstimator(
            CostEstimationConfig config,
            StatisticsProviderFactory statisticsProviderFactory,
            OntologyProvider ontologyProvider) {
        super(estimators(config, statisticsProviderFactory, ontologyProvider));
    }
    //endregion
}
