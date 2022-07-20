package org.opensearch.graph.epb.plan;

import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.epb.plan.estimation.pattern.PredicateCostEstimator;
import org.opensearch.graph.epb.plan.estimation.pattern.RegexPatternCostEstimator;
import org.opensearch.graph.epb.plan.extenders.M1.M1DfsRedundantPlanExtensionStrategy;
import org.opensearch.graph.epb.plan.pruners.CheapestPlanPruneStrategy;
import org.opensearch.graph.epb.plan.pruners.NoPruningPruneStrategy;
import org.opensearch.graph.epb.plan.selectors.AllCompletePlanSelector;
import org.opensearch.graph.epb.plan.validation.M1PlanValidator;
import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.DoubleCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.Rel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.opensearch.graph.epb.utils.DfsTestUtils.buildSchemaProvider;
import static org.opensearch.graph.epb.utils.DfsTestUtils.ruleBaseEstimator;
import static org.opensearch.graph.model.OntologyTestUtils.OWN;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DfsRuleBasedBottomUpPlanSearcherNoQuantTests {
    //region Fields
    private OntologyProvider ontologyProvider;
    //ontology
    private Ontology.Accessor ont = new Ontology.Accessor(OntologyTestUtils.createDragonsOntologyShort());
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    //endregion

    @Before
    public void setup(){
        this.ontologyProvider = mock(OntologyProvider.class);
        when(ontologyProvider.get(any())).thenReturn(Optional.of(OntologyTestUtils.createDragonsOntologyShort()));

        this.schemaProviderFactory = ontology -> buildSchemaProvider(new Ontology.Accessor(ontology));
    }

    @Test
    public void TestBuilderSingleEntity() {
        //Start[0]:EEntityBase[1]:Relation[2]:EEntityBase[3]
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, OntologyTestUtils.PERSON.type)).
                next(ePropGroup(2)).build();
        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();
        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        Assert.assertEquals(2, plan.getPlan().getOps().size());
        Assert.assertEquals("Plan[[EntityOp(Asg(ETyped(1))):EntityFilterOp(Asg(EPropGroup(2)))]]", plan.getPlan().toString());
        Assert.assertEquals(new DoubleCost(80.0), plan.getCost().getGlobalCost());
    }

    @Test
    public void TestBuilderShouldStartWithFirstStep() {
        //Start[0]:EEntityBase[1]:Relation[2]:EEntityBase[3]
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(concrete(1, "eId0000123", OntologyTestUtils.PERSON.type, "Jomala", "person")).
                next(ePropGroup(2)).
                next(rel(3, OWN.getrType(), Rel.Direction.R).below(relProp(4))).
                next(typed(5, OntologyTestUtils.DRAGON.type)).
                next(ePropGroup(6)).build();

        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();
        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        Assert.assertEquals(6, plan.getPlan().getOps().size());
        Assert.assertEquals(1, ((EntityOp) plan.getPlan().getOps().get(0)).getAsgEbase().geteNum());
        Assert.assertEquals("Plan[[EntityOp(Asg(EConcrete(1))):EntityFilterOp(Asg(EPropGroup(2))):RelationOp(Asg(Rel(3))):RelationFilterOp(Asg(RelPropGroup(4))):EntityOp(Asg(ETyped(5))):EntityFilterOp(Asg(EPropGroup(6)))]]", plan.getPlan().toString());
        Assert.assertEquals(new DoubleCost(1.0), plan.getCost().getGlobalCost());
    }

    @Test
    public void TestBuilderShouldStartWithLastStep() {
        //Start[0]:EEntityBase[1]:Relation[2]:EEntityBase[3]
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, OntologyTestUtils.DRAGON.type)).
                next(ePropGroup(2)).
                next(rel(3, OWN.getrType(), Rel.Direction.R).below(relProp(4))).
                next(concrete(5, "eId0000123", OntologyTestUtils.PERSON.type, "Jomala", "person")).
                next(ePropGroup(6)).build();

        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();
        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        Assert.assertEquals(6, plan.getPlan().getOps().size());
        Assert.assertEquals(5, ((EntityOp) plan.getPlan().getOps().get(0)).getAsgEbase().geteNum());
        Assert.assertEquals("Plan[[EntityOp(Asg(EConcrete(5))):EntityFilterOp(Asg(EPropGroup(6))):RelationOp(Asg(Rel(3))):RelationFilterOp(Asg(RelPropGroup(4))):EntityOp(Asg(ETyped(1))):EntityFilterOp(Asg(EPropGroup(2)))]]", plan.getPlan().toString());
        Assert.assertEquals(new DoubleCost(1.0), plan.getCost().getGlobalCost());
    }

    @Test
    public void TestBuilderShouldStartWithMiddleStep() {
        //Start[0]:EEntityBase[1]:EPropGroup[2]:==>Relation[3]:RelPropGroup[4]:==>EEntityBase[5]:EPropGroup[6]:==>Relation[7]:RelPropGroup[8]:==>EEntityBase[9]:EPropGroup[10]
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, OntologyTestUtils.PERSON.type)).
                next(ePropGroup(2)).
                next(rel(3, OWN.getrType(), Rel.Direction.R).below(relProp(4))).
                next(concrete(5, "eId0000123", OntologyTestUtils.PERSON.type, "Jomala", "person")).
                next(ePropGroup(6)).
                next(rel(7, OWN.getrType(), Rel.Direction.R).below(relProp(8))).
                next(typed(9, OntologyTestUtils.DRAGON.type)).
                next(ePropGroup(10)).
                build();
        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();
        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        Assert.assertEquals(11, plan.getPlan().getOps().size());
        Assert.assertEquals(5, ((EntityOp) plan.getPlan().getOps().get(0)).getAsgEbase().geteNum());
        Assert.assertEquals("Plan[[EntityOp(Asg(EConcrete(5))):EntityFilterOp(Asg(EPropGroup(6))):RelationOp(Asg(Rel(7))):RelationFilterOp(Asg(RelPropGroup(8))):EntityOp(Asg(ETyped(9))):EntityFilterOp(Asg(EPropGroup(10))):GoToEntityOp(Asg(EConcrete(5))):RelationOp(Asg(Rel(3))):RelationFilterOp(Asg(RelPropGroup(4))):EntityOp(Asg(ETyped(1))):EntityFilterOp(Asg(EPropGroup(2)))]]", plan.getPlan().toString());
        Assert.assertEquals(new DoubleCost(1.0), plan.getCost().getGlobalCost());
    }
    @Test
    public void TestBuilderShouldStartWithEndStep() {
        //Start[0]:EEntityBase[1]:EPropGroup[2]:==>Relation[3]:RelPropGroup[4]:==>EEntityBase[5]:EPropGroup[6]:==>Relation[7]:RelPropGroup[8]:==>EEntityBase[9]:EPropGroup[10]
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, OntologyTestUtils.PERSON.type)).
                next(ePropGroup(2)).
                next(rel(3, OWN.getrType(), Rel.Direction.R).below(relProp(4))).
                next(typed(5, OntologyTestUtils.DRAGON.type)).
                next(ePropGroup(6)).
                next(rel(7, OWN.getrType(), Rel.Direction.R).below(relProp(8))).
                next(concrete(9, "eId0000123", OntologyTestUtils.PERSON.type, "Jomala", "person")).
                next(ePropGroup(10)).
                build();
        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();
        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        Assert.assertEquals(10, plan.getPlan().getOps().size());
        Assert.assertEquals(9, ((EntityOp) plan.getPlan().getOps().get(0)).getAsgEbase().geteNum());
        Assert.assertEquals("Plan[[EntityOp(Asg(EConcrete(9))):EntityFilterOp(Asg(EPropGroup(10))):RelationOp(Asg(Rel(7))):RelationFilterOp(Asg(RelPropGroup(8))):EntityOp(Asg(ETyped(5))):EntityFilterOp(Asg(EPropGroup(6))):RelationOp(Asg(Rel(3))):RelationFilterOp(Asg(RelPropGroup(4))):EntityOp(Asg(ETyped(1))):EntityFilterOp(Asg(EPropGroup(2)))]]", plan.getPlan().toString());
        Assert.assertEquals(new DoubleCost(1.0), plan.getCost().getGlobalCost());
    }


    private BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> createBottomUpPlanSearcher() {

        return new BottomUpPlanSearcher<>(
                new M1DfsRedundantPlanExtensionStrategy(
                        ontologyProvider,
                        schemaProviderFactory),
                new NoPruningPruneStrategy<>(),
                new CheapestPlanPruneStrategy(),
                new AllCompletePlanSelector<>(),
                new AllCompletePlanSelector<>(),
                new M1PlanValidator(),
                new PredicateCostEstimator<>(plan -> plan.getOps().size() <= 2,
                        new RegexPatternCostEstimator(ruleBaseEstimator(ont)),
                        (plan, context) -> new PlanWithCost<>(plan, context.getPreviousCost().get().getCost())));
    }
}
