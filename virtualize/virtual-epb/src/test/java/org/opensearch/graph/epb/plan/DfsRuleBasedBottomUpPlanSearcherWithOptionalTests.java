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
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.RelProp;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.opensearch.graph.epb.utils.DfsTestUtils.buildSchemaProvider;
import static org.opensearch.graph.epb.utils.DfsTestUtils.ruleBaseEstimator;
import static org.opensearch.graph.model.OntologyTestUtils.OWN;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.query.properties.constraint.Constraint.of;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.eq;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.gt;
import static org.opensearch.graph.model.query.Rel.Direction.R;
import static org.opensearch.graph.model.query.quant.QuantType.all;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DfsRuleBasedBottomUpPlanSearcherWithOptionalTests {
    //region Fields
    private OntologyProvider ontologyProvider;
    //ontology
    private Ontology.Accessor ont = new Ontology.Accessor(OntologyTestUtils.createDragonsOntologyShort());
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    //endregion

    @Before
    public void setup() {
        this.ontologyProvider = mock(OntologyProvider.class);
        when(ontologyProvider.get(any())).thenReturn(Optional.of(OntologyTestUtils.createDragonsOntologyShort()));

        this.schemaProviderFactory = ontology -> buildSchemaProvider(new Ontology.Accessor(ontology));
    }


    @Test
    public void TestBuilderEntityStartStep() {
        //Start[0]:EEntityBase[1]:EPropGroup[101]:==>Relation[2]:RelPropGroup[201]:==>EEntityBase[3]:Quant1[4]:{301|5|7}:EPropGroup[301]:==>Relation[5]:RelPropGroup[501]:==>EEntityBase[6]:EPropGroup[601]:OptionalComp[7]:==>Relation[8]:RelPropGroup[801]:==>EEntityBase[9]:EPropGroup[901]
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(concrete(1, "eId00001", OntologyTestUtils.PERSON.type, "entity1", "A"))
                .next(ePropGroup(101))
                .next(rel(2, OWN.getrType(), R)
                        .below(relProp(201, RelProp.of(2, "2", of(eq, "value2")))))
                .next(typed(3, OntologyTestUtils.PERSON.type, "B"))
                .next(quant1(4, all))
                .in(
                        ePropGroup(301, EProp.of(3, "type", of(eq, "value1")),EProp.of(3, "type", of(gt, "value3"))),
                        rel(5, OWN.getrType(), R).below(relProp(501))
                                .next(typed(6, OntologyTestUtils.PERSON.type, "C")
                                        .next(ePropGroup(601))),
                        optional(7)
                                .next(rel(8, OWN.getrType(), R).below(relProp(801))
                                .next(typed(9, OntologyTestUtils.PERSON.type, "E").next(ePropGroup(901)))))
                .build();
        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();
        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        Assert.assertEquals(12, plan.getPlan().getOps().size());
        Assert.assertEquals(1, ((EntityOp) plan.getPlan().getOps().get(0)).getAsgEbase().geteNum());
        Assert.assertEquals("Plan[[EntityOp(Asg(EConcrete(1))):EntityFilterOp(Asg(EPropGroup(101))):RelationOp(Asg(Rel(2))):RelationFilterOp(Asg(RelPropGroup(201))):EntityOp(Asg(ETyped(3))):EntityFilterOp(Asg(EPropGroup(301))):RelationOp(Asg(Rel(5))):RelationFilterOp(Asg(RelPropGroup(501))):EntityOp(Asg(ETyped(6))):EntityFilterOp(Asg(EPropGroup(601))):GoToEntityOp(Asg(ETyped(3))):OptionalOp[[EntityNoOp(Asg(ETyped(3))):RelationOp(Asg(Rel(8))):RelationFilterOp(Asg(RelPropGroup(801))):EntityOp(Asg(ETyped(9))):EntityFilterOp(Asg(EPropGroup(901)))]]]]", plan.getPlan().toString());
        Assert.assertEquals(new DoubleCost(1.0), plan.getCost().getGlobalCost());
    }

    @Test
    public void TestBuilderSingleEntityLastStepButInsideOptional() {
        //Start[0]:EEntityBase[1]:EPropGroup[101]:==>Relation[2]:RelPropGroup[201]:==>EEntityBase[3]:Quant1[4]:{301|5|7}:EPropGroup[301]:==>Relation[5]:RelPropGroup[501]:==>EEntityBase[6]:EPropGroup[601]:OptionalComp[7]:==>Relation[8]:RelPropGroup[801]:==>EEntityBase[9]:EPropGroup[901]
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, OntologyTestUtils.PERSON.type, "E"))
                .next(ePropGroup(101))
                .next(rel(2, OWN.getrType(), R)
                        .below(relProp(201, RelProp.of(2, "2", of(eq, "value2")))))
                .next(typed(3, OntologyTestUtils.PERSON.type, "B"))
                .next(quant1(4, all))
                .in(
                        ePropGroup(301, EProp.of(3, "type", of(eq, "value1")),EProp.of(3, "type", of(gt, "value3"))),
                        rel(5, OWN.getrType(), R).below(relProp(501))
                                .next(typed(6, OntologyTestUtils.PERSON.type, "C")
                                        .next(ePropGroup(601))),
                        optional(7)
                                .next(rel(8, OWN.getrType(), R).below(relProp(801))
                                .next(concrete(9, "eId00001", OntologyTestUtils.PERSON.type, "entity1", "A").next(ePropGroup(901)))))
                .build();
        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();
        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        Assert.assertEquals(12, plan.getPlan().getOps().size());
        Assert.assertEquals(6, ((EntityOp) plan.getPlan().getOps().get(0)).getAsgEbase().geteNum());
        Assert.assertEquals("Plan[[EntityOp(Asg(ETyped(6))):EntityFilterOp(Asg(EPropGroup(601))):RelationOp(Asg(Rel(5))):RelationFilterOp(Asg(RelPropGroup(501))):EntityOp(Asg(ETyped(3))):EntityFilterOp(Asg(EPropGroup(301))):OptionalOp[[EntityNoOp(Asg(ETyped(3))):RelationOp(Asg(Rel(8))):RelationFilterOp(Asg(RelPropGroup(801))):EntityOp(Asg(EConcrete(9))):EntityFilterOp(Asg(EPropGroup(901)))]]:GoToEntityOp(Asg(ETyped(3))):RelationOp(Asg(Rel(2))):RelationFilterOp(Asg(RelPropGroup(201))):EntityOp(Asg(ETyped(1))):EntityFilterOp(Asg(EPropGroup(101)))]]", plan.getPlan().toString());
        Assert.assertEquals(new DoubleCost(80.0), plan.getCost().getGlobalCost());
    }

    @Test
    public void TestBuilderSingleEntityMiddleStep() {
        //Start[0]:EEntityBase[1]:EPropGroup[101]:==>Relation[2]:RelPropGroup[201]:==>EEntityBase[3]:Quant1[4]:{301|5|7}:EPropGroup[301]:==>Relation[5]:RelPropGroup[501]:==>EEntityBase[6]:EPropGroup[601]:OptionalComp[7]:==>Relation[8]:RelPropGroup[801]:==>EEntityBase[9]:EPropGroup[901]
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, OntologyTestUtils.PERSON.type, "E"))
                .next(ePropGroup(101))
                .next(rel(2, OWN.getrType(), R)
                        .below(relProp(201, RelProp.of(2, "2", of(eq, "value2")))))
                .next(typed(3, OntologyTestUtils.PERSON.type, "B"))
                .next(quant1(4, all))
                .in(
                        ePropGroup(301, EProp.of(3, "type", of(eq, "value1")),EProp.of(3, "type", of(gt, "value3"))),
                        rel(5, OWN.getrType(), R).below(relProp(501))
                                .next(concrete(6, "eId00001", OntologyTestUtils.PERSON.type, "entity1", "A")
                                        .next(ePropGroup(601))),
                        optional(7)
                                .next(rel(8, OWN.getrType(), R).below(relProp(801))
                                .next(concrete(9, "eId00001", OntologyTestUtils.PERSON.type, "entity1", "A").next(ePropGroup(901)))))
                .build();
        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();
        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        Assert.assertEquals(12, plan.getPlan().getOps().size());
        Assert.assertEquals(6, ((EntityOp) plan.getPlan().getOps().get(0)).getAsgEbase().geteNum());
        Assert.assertEquals("Plan[[EntityOp(Asg(EConcrete(6))):EntityFilterOp(Asg(EPropGroup(601))):RelationOp(Asg(Rel(5))):RelationFilterOp(Asg(RelPropGroup(501))):EntityOp(Asg(ETyped(3))):EntityFilterOp(Asg(EPropGroup(301))):OptionalOp[[EntityNoOp(Asg(ETyped(3))):RelationOp(Asg(Rel(8))):RelationFilterOp(Asg(RelPropGroup(801))):EntityOp(Asg(EConcrete(9))):EntityFilterOp(Asg(EPropGroup(901)))]]:GoToEntityOp(Asg(ETyped(3))):RelationOp(Asg(Rel(2))):RelationFilterOp(Asg(RelPropGroup(201))):EntityOp(Asg(ETyped(1))):EntityFilterOp(Asg(EPropGroup(101)))]]", plan.getPlan().toString());
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
