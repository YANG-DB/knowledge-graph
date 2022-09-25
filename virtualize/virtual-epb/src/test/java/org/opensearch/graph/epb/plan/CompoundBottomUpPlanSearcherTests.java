package org.opensearch.graph.epb.plan;

import org.opensearch.graph.dispatcher.epb.PlanPruneStrategy;
import org.opensearch.graph.dispatcher.epb.PlanSelector;
import org.opensearch.graph.dispatcher.epb.PlanValidator;
import org.opensearch.graph.dispatcher.epb.CostEstimator;
import org.opensearch.graph.epb.plan.estimation.dummy.DummyCostEstimator;
import org.opensearch.graph.epb.plan.estimation.IncrementalEstimationContext;
import org.opensearch.graph.epb.plan.extenders.M1.M1NonRedundantPlanExtensionStrategy;
import org.opensearch.graph.epb.plan.pruners.NoPruningPruneStrategy;
import org.opensearch.graph.epb.plan.selectors.AllCompletePlanSelector;
import org.opensearch.graph.epb.plan.validation.M1PlanValidator;
import org.opensearch.graph.epb.utils.BuilderTestUtil;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.AsgEBaseContainer;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.ontology.Value;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.properties.EProp;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.opensearch.graph.model.OntologyTestUtils.*;
import static org.opensearch.graph.model.OntologyTestUtils.Gender.MALE;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.*;
import static org.opensearch.graph.model.query.Rel.Direction.R;
import static org.opensearch.graph.model.query.properties.RelProp.of;
import static org.opensearch.graph.model.query.quant.QuantType.all;

/**
 * Created by moti on 2/23/2017.
 */
public class CompoundBottomUpPlanSearcherTests {

    public static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        long time = System.currentTimeMillis();
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, PERSON.type)
                        .next(ePropGroup(2,EProp.of(3, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189)))))
                .next(rel(4, OWN.getrType(), R)
                        .below(relProp(5, of(6, START_DATE.type, Constraint.of(eq, new Date())))))
                .next(typed(7, DRAGON.type))
                .next(quant1(8, all))
                .in(ePropGroup(9, EProp.of(10, NAME.type, Constraint.of(eq, "smith")), EProp.of(11, GENDER.type, Constraint.of(gt, new Value(MALE.ordinal(),MALE.name()))))
                        , rel(12, FREEZE.getrType(), R)
                                .below(relProp(122))
                                .next(unTyped(13)
                                        .next(ePropGroup(14,EProp.of(15, NAME.type, Constraint.of(ConstraintOp.notContains, "bob"))))
                                )
                        , rel(16, FIRE.getrType(), R)
                                .below(relProp(18, of(19, START_DATE.type,
                                        Constraint.of(ge, new Date(time - 1000 * 60))),
                                        of(19, END_DATE.type, Constraint.of(le, new Date(time + 1000 * 60)))))
                                .next(concrete(20, "smoge", DRAGON.type, "Display:smoge", "D")
                                        .next(ePropGroup(21,EProp.of(22, NAME.type, Constraint.of(ConstraintOp.eq, "smoge"))))
                                )
                )
                .build();
    }

    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, OntologyTestUtils.PERSON.type,"A"))
                .next(rel(2,OWN.getrType(),R))
                .next(typed(3, OntologyTestUtils.DRAGON.type,"B")).build();
    }

    @Test
    public void TestBuilderSimpleQ1Path(){
        AsgQuery query = simpleQuery1("name", "o1");

        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();


        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);

        Assert.assertEquals(3, plan.getPlan().getOps().size());
    }

    @Test
    public void TestBuilderSimplePath(){
        Pair<AsgQuery, AsgEBase<? extends EBase>> query = BuilderTestUtil.createTwoEntitiesPathQuery();

        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();


        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query.getLeft());

        Assert.assertEquals(3, plan.getPlan().getOps().size());
    }

    @Test
    public void TestBuilderSingleEntity(){

        Pair<AsgQuery, AsgEBase> query = BuilderTestUtil.createSingleEntityQuery();

        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();


        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query.getLeft());

        Assert.assertEquals(1, plan.getPlan().getOps().size());
    }

    @Test
    public void TestBuilderAllPaths() {
        Pair<AsgQuery, AsgEBase<? extends EBase>> query = BuilderTestUtil.createTwoEntitiesPathQuery();

        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();


        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query.getLeft());

        Assert.assertEquals(3, plan.getPlan().getOps().size());

        AsgEBase firstElement = query.getLeft().getStart().getNext().get(0);
        AsgEBase secondElement = (AsgEBase) firstElement.getNext().get(0);
        AsgEBase thirdElement = (AsgEBase) secondElement.getNext().get(0);

        List<PlanOp> ops = plan.getPlan().getOps();
        Assert.assertTrue(firstElement.geteNum() == ((AsgEBaseContainer) ops.get(0)).getAsgEbase().geteNum() &&
                secondElement.geteNum() == ((AsgEBaseContainer) ops.get(1)).getAsgEbase().geteNum() &&
                thirdElement.geteNum() == ((AsgEBaseContainer) ops.get(2)).getAsgEbase().geteNum());
    }

    private BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> createBottomUpPlanSearcher() {

        PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> pruneStrategy = new NoPruningPruneStrategy<>();
        PlanValidator<Plan, AsgQuery> validator = new M1PlanValidator();

        CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> costEstimator =
                new DummyCostEstimator<>(new PlanDetailedCost());

        PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> planSelector = new AllCompletePlanSelector<>();

        return new BottomUpPlanSearcher<>(
                new M1NonRedundantPlanExtensionStrategy(),
                pruneStrategy,
                pruneStrategy,
                planSelector,
                planSelector,
                validator,
                costEstimator);
    }


}
