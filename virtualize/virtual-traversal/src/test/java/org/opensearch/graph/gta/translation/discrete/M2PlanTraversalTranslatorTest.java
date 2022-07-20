package org.opensearch.graph.gta.translation.discrete;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opensearch.graph.dispatcher.gta.PlanTraversalTranslator;
import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.executor.ontology.UniGraphProvider;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.CountEstimatesCost;
import org.opensearch.graph.model.execution.plan.costs.DoubleCost;
import org.opensearch.graph.model.execution.plan.costs.JoinCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.ontology.EntityType;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.Property;
import org.opensearch.graph.model.ontology.RelationshipType;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.Start;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.ScoreEProp;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.opensearch.graph.unipop.process.JoinStep;
import org.opensearch.graph.unipop.promise.PromiseGraph;
import org.unipop.structure.UniGraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opensearch.graph.model.query.Rel.Direction.R;

public class M2PlanTraversalTranslatorTest {

    UniGraphProvider uniGraphProvider;
    PlanTraversalTranslator translator;

    @Before
    public void setUp() throws Exception {
        translator = new M2PlanTraversalTranslator();
        UniGraph uniGraph = mock(UniGraph.class);
        when(uniGraph.traversal()).thenReturn(new GraphTraversalSource(uniGraph));
        this.uniGraphProvider = mock(UniGraphProvider.class);
        when(uniGraphProvider.getGraph(any())).thenReturn(uniGraph);
    }

    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        Start start = new Start();
        start.seteNum(0);

        ETyped eTyped = new ETyped();
        eTyped.seteNum(1);
        eTyped.seteTag("A");
        eTyped.seteType("1");

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setDir(R);
        rel.setrType("1");

        ETyped eTyped2 = new ETyped();
        eTyped2.seteNum(3);
        eTyped2.seteTag("B");
        eTyped2.seteType("2");

        AsgEBase<Start> asgStart =
                AsgEBase.Builder.<Start>get().withEBase(start)
                        .withNext(AsgEBase.Builder.get().withEBase(eTyped)
                                .withNext(AsgEBase.Builder.get().withEBase(rel)
                                        .withNext(AsgEBase.Builder.get().withEBase(eTyped2)
                                                .build())
                                        .build())
                                .build())
                        .build();

        return AsgQuery.AsgQueryBuilder.anAsgQuery().withName(queryName).withOnt(ontologyName).withStart(asgStart).build();
    }

    public static AsgQuery simpleBoostingQuery(String queryName, String ontologyName) {
        Start start = new Start();
        start.seteNum(0);

        ETyped eTyped = new ETyped();
        eTyped.seteNum(1);
        eTyped.seteTag("A");
        eTyped.seteType("1");

        EPropGroup group = new EPropGroup(2);
        group.getProps().add(new ScoreEProp(2, "stringValue", Constraint.of(ConstraintOp.eq, "abc"), 100));

        AsgEBase<Start> asgStart =
                AsgEBase.Builder.<Start>get().withEBase(start)
                        .withNext(AsgEBase.Builder.get().withEBase(eTyped)
                                .withNext(AsgEBase.Builder.get().withEBase(group).build())
                                .build())
                        .build();

        return AsgQuery.AsgQueryBuilder.anAsgQuery().withName(queryName).withOnt(ontologyName).withStart(asgStart).build();
    }

    @Test
    public void simpleJoinTranslationTest(){
        AsgQuery asgQuery = simpleQuery1("q","o");
        Plan leftBranch = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 1)));
        Plan rightBranch = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 3)), new RelationOp(AsgQueryUtil.element$(asgQuery, 2), Rel.Direction.L), new EntityOp(AsgQueryUtil.element$(asgQuery, 1)));
        PlanDetailedCost leftCost = new PlanDetailedCost(new DoubleCost(0), Collections.singleton(new PlanWithCost<>(leftBranch, new CountEstimatesCost(0, 100))));
        PlanDetailedCost rightCost = new PlanDetailedCost(new DoubleCost(0), Collections.singleton(new PlanWithCost<>(rightBranch, new CountEstimatesCost(0, 100))));
        EntityJoinOp joinOp = new EntityJoinOp(leftBranch, rightBranch );
        PlanDetailedCost joinCost = new PlanDetailedCost(new DoubleCost(0), Collections.singleton(new PlanWithCost<Plan, CountEstimatesCost>(new Plan(joinOp), new JoinCost(0, 0, leftCost, rightCost))));
        Ontology.Accessor ont = getOntologyAccessor();
        Traversal actualTraversal = translator.translate(new PlanWithCost<>(new Plan(joinOp), joinCost), new TranslationContext(ont, new PromiseGraph().traversal()));
        GraphTraversal<?, ?> leftTraversal = this.translator.translate(new PlanWithCost<>(leftBranch, leftCost), new TranslationContext(ont, new PromiseGraph().traversal()));
        GraphTraversal<?, ?> rightTraversal = this.translator.translate(new PlanWithCost<>(rightBranch, rightCost), new TranslationContext(ont, new PromiseGraph().traversal()));
        JoinStep<Object, Element> joinStep = new JoinStep<>(__.start().asAdmin());
        joinStep.addLocalChild(leftTraversal.asAdmin());
        joinStep.addLocalChild(rightTraversal.asAdmin());
        Assert.assertEquals(__.start().asAdmin().addStep(joinStep), actualTraversal);
    }

    @Test
    public void hierarchicalJoinTranslationTest(){
        AsgQuery asgQuery = simpleQuery1("q","o");
        Plan branch = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 1)));

        PlanDetailedCost cost = new PlanDetailedCost(new DoubleCost(0), Collections.singleton(new PlanWithCost<>(branch, new CountEstimatesCost(0, 100))));

        EntityJoinOp innerJoin = new EntityJoinOp(branch, branch);
        PlanDetailedCost innerJoinCost = new PlanDetailedCost(new DoubleCost(0), Collections.singleton(new PlanWithCost<>(new Plan(innerJoin), new JoinCost(0, 100, cost, cost))));
        EntityJoinOp topJoinOp = new EntityJoinOp(new Plan(innerJoin), branch);
        PlanDetailedCost topJoinCost = new PlanDetailedCost(new DoubleCost(0), Collections.singleton(new PlanWithCost<Plan, CountEstimatesCost>(new Plan(topJoinOp), new JoinCost(0, 0, innerJoinCost, cost))));
        Ontology.Accessor ont = getOntologyAccessor();
        Traversal actualTraversal = translator.translate(new PlanWithCost<>(new Plan(topJoinOp), topJoinCost), new TranslationContext(ont, new PromiseGraph().traversal()));
        GraphTraversal<?, ?> innerTraversal = this.translator.translate(new PlanWithCost<>(new Plan(innerJoin), innerJoinCost), new TranslationContext(ont, new PromiseGraph().traversal()));
        GraphTraversal<?, ?> rightTraversal = this.translator.translate(new PlanWithCost<>(branch, cost), new TranslationContext(ont, new PromiseGraph().traversal()));
        JoinStep<Object, Element> joinStep = new JoinStep<>(__.start().asAdmin());
        joinStep.addLocalChild(innerTraversal.asAdmin());
        joinStep.addLocalChild(rightTraversal.asAdmin());
        Assert.assertEquals(__.start().asAdmin().addStep(joinStep), actualTraversal);
    }

    @Test
    public void boostingTest(){
        AsgQuery asgQuery = simpleBoostingQuery("q","o");
        Plan plan = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery,1)), new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 2)));

        PlanDetailedCost cost = new PlanDetailedCost(new DoubleCost(0), Collections.singleton(new PlanWithCost<>(plan, new CountEstimatesCost(0, 100))));
        Ontology.Accessor ont = getOntologyAccessor();
        GraphTraversal<?, ?> translate = translator.translate(new PlanWithCost<>(plan, cost), new TranslationContext(ont, new PromiseGraph().traversal()));

        int x = 2;

    }

    private Ontology.Accessor getOntologyAccessor() {
        Ontology ontology = Mockito.mock(Ontology.class);
        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<EntityType> entityTypes = new ArrayList<>();
                    entityTypes.add(EntityType.Builder.get()
                            .withEType("1").withName("Person").build());
                    entityTypes.add(EntityType.Builder.get()
                            .withEType("2").withName("Dragon").build());
                    return  entityTypes;
                }
        );
        when(ontology.getRelationshipTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<RelationshipType> relTypes = new ArrayList<>();
                    relTypes.add(RelationshipType.Builder.get()
                            .withRType("1").withName("Fire").build());
                    return  relTypes;
                }
        );
        when(ontology.getProperties()).thenAnswer(invocationOnMock ->
                new HashSet<>(Arrays.asList(new Property("stringValue", "stringValue", "string"))));

        return new Ontology.Accessor(ontology);
    }
}
