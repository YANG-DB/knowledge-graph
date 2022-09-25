package org.opensearch.graph.epb.plan;

import org.opensearch.graph.dispatcher.epb.PlanPruneStrategy;
import org.opensearch.graph.dispatcher.epb.PlanSelector;
import org.opensearch.graph.dispatcher.epb.PlanValidator;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.epb.plan.estimation.CostEstimationConfig;
import org.opensearch.graph.epb.plan.estimation.pattern.RegexPatternCostEstimator;
import org.opensearch.graph.epb.plan.estimation.pattern.estimators.M1PatternCostEstimator;
import org.opensearch.graph.epb.plan.extenders.M1.M1NonRedundantPlanExtensionStrategy;
import org.opensearch.graph.epb.plan.pruners.NoPruningPruneStrategy;
import org.opensearch.graph.epb.plan.selectors.AllCompletePlanSelector;
import org.opensearch.graph.epb.plan.statistics.EBaseStatisticsProvider;
import org.opensearch.graph.epb.plan.validation.M1PlanValidator;
import org.opensearch.graph.epb.utils.BasicScenarioSetup;
import org.opensearch.graph.epb.utils.ScenarioMockUtil;
import org.opensearch.graph.model.OntologyTestUtils.PERSON;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.CountEstimatesCost;
import org.opensearch.graph.model.execution.plan.costs.DoubleCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.opensearch.graph.model.OntologyTestUtils.FIRST_NAME;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.typed;

/**
 * Created by moti on 5/18/2017.
 */
public class SmartEpbTests {

    private BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher;

    @Before
    public void setup(){
        ScenarioMockUtil scenarioMockUtil = BasicScenarioSetup.setup();
        EBaseStatisticsProvider eBaseStatisticsProvider = new EBaseStatisticsProvider(
                scenarioMockUtil.getGraphElementSchemaProvider(),
                scenarioMockUtil.getOntologyAccessor(),
                scenarioMockUtil.getGraphStatisticsProvider());

        RegexPatternCostEstimator estimator = new RegexPatternCostEstimator(new M1PatternCostEstimator(
                new CostEstimationConfig(1.0, 0.001),
                (ont) -> eBaseStatisticsProvider,
                new OntologyProvider() {
                    @Override
                    public Optional<Ontology> get(String id) {
                        return Optional.of(scenarioMockUtil.getOntologyAccessor().get());
                    }

                    @Override
                    public Collection<Ontology> getAll() {
                        return Collections.singleton(scenarioMockUtil.getOntologyAccessor().get());
                    }

                    @Override
                    public Ontology add(Ontology ontology) {
                return ontology;
             }
                }));

        PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> pruneStrategy = new NoPruningPruneStrategy<>();
        PlanValidator<Plan, AsgQuery> validator = new M1PlanValidator();


        PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> planSelector = new AllCompletePlanSelector<>();

        planSearcher = new BottomUpPlanSearcher<>(
                new M1NonRedundantPlanExtensionStrategy(),
                pruneStrategy,
                pruneStrategy,
                planSelector,
                planSelector,
                validator,
                estimator);
    }

    @Test
    public void testSingleElement(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(AsgQuery.Builder.ePropGroup(2,EProp.of(2, FIRST_NAME.type, Constraint.of(ConstraintOp.eq, "abc")))).
                build();

        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);

        Assert.assertNotNull(plan);
        Assert.assertEquals(plan.getCost().getGlobalCost(),new DoubleCost(10));
        Assert.assertEquals(new CountEstimatesCost(10, 10), plan.getCost().getPlanStepCosts().iterator().next().getCost());
    }
}
