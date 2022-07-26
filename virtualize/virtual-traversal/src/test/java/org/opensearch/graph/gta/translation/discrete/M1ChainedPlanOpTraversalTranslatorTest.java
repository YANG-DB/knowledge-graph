package org.opensearch.graph.gta.translation.discrete;

import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.executor.ontology.UniGraphProvider;
import org.opensearch.graph.gta.strategy.discrete.M1PlanOpTranslationStrategy;
import org.opensearch.graph.gta.translation.ChainedPlanOpTraversalTranslator;
import org.opensearch.graph.dispatcher.gta.PlanTraversalTranslator;
import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.*;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationFilterOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.ontology.EntityType;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.RelationshipType;
import org.opensearch.graph.model.query.Rel;

import org.opensearch.graph.model.query.properties.constraint.CountConstraintOp;
import org.opensearch.graph.model.query.quant.QuantType;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.unipop.promise.Constraint;
import org.opensearch.graph.unipop.promise.PromiseGraph;
import org.opensearch.graph.unipop.structure.SearchUniGraph;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.opensearch.graph.unipop.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.util.DefaultTraversalStrategies;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.unipop.query.controller.ControllerManager;
import org.unipop.query.controller.UniQueryController;
import org.unipop.structure.UniGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Roman on 14/05/2017.
 */
public class M1ChainedPlanOpTraversalTranslatorTest {
    UniGraphProvider uniGraphProvider;
    PlanTraversalTranslator translator;

    @Before
    public void setUp() throws Exception {
        translator = new ChainedPlanOpTraversalTranslator(new M1PlanOpTranslationStrategy());
        UniGraph uniGraph = new SearchUniGraph(null, graph -> new ControllerManager() {
            @Override
            public Set<UniQueryController> getControllers() {
                return Collections.emptySet();
            }

            @Override
            public void close() {

            }
        }, DefaultTraversalStrategies::new);

        this.uniGraphProvider = mock(UniGraphProvider.class);
        when(uniGraphProvider.getGraph(any())).thenReturn(uniGraph);
    }

    @Test
    public void test_concrete_rel_untyped() throws Exception {
        Plan plan = create_Con_Rel_Unt_PathQuery();
        Ontology.Accessor ont = getOntologyAccessor();
        Traversal actualTraversal = translator.translate(new PlanWithCost<>(plan, null), new TranslationContext(ont, this.uniGraphProvider.getGraph(ont.get()).traversal()));

        Traversal expectedTraversal =
                __.start().V().as("A")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().and(
                                __.start().has(T.id, "12345678"),
                                __.start().has(T.label, "Person"))))
                        .outE().as("A-->B")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().has(T.label, "Fire")))
                        .otherV().as("B");

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    @Ignore("FIXME")
    public void FIXME_test_concrete_rel_typed_Agg() throws Exception {
        Plan plan = create_Con_Rel_Typ_Agg_PathQuery();
        Ontology.Accessor ont = getOntologyAccessor();
        Traversal actualTraversal = translator.translate(new PlanWithCost<>(plan, null),
                new TranslationContext(ont, new PromiseGraph().traversal()));

        Traversal expectedTraversal =
                new PromiseGraph().traversal().V().as("A")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().and(
                                __.start().has(T.id, "12345678"),
                                __.start().has(T.label, "Person"))))
                        .outE().as("A-->B")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().has(T.label, "Fire")))
                        .otherV().as("B");

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void test_concrete_rel_typed() throws Exception {
        Plan plan = create_Con_Rel_Typ_PathQuery();
        Ontology.Accessor ont = getOntologyAccessor();
        Traversal actualTraversal = translator.translate(new PlanWithCost<>(plan, null), new TranslationContext(ont, new PromiseGraph().traversal()));

        Traversal expectedTraversal =
                new PromiseGraph().traversal().V().as("A")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().and(
                                __.start().has(T.id, "12345678"),
                                __.start().has(T.label, "Person"))))
                        .outE().as("A-->B")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().has(T.label, "Fire")))
                        .otherV().as("B");

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void test_concrete_rel_concrete() throws Exception {
        Plan plan = create_Con_Rel_Con_PathQuery();
        Ontology.Accessor ont = getOntologyAccessor();
        Traversal actualTraversal = translator.translate(new PlanWithCost<>(plan, null), new TranslationContext(ont, new PromiseGraph().traversal()));

        Traversal expectedTraversal =
                new PromiseGraph().traversal().V().as("A")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().and(
                                __.start().has(T.id, "12345678"),
                                __.start().has(T.label, "Person"))))
                        .outE().as("A-->B")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().has(T.label, "Fire")))
                        .otherV().as("B")
                            .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().and(
                                __.start().has(T.id, "87654321"),
                                __.start().has(T.label, "Person"))));

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void test_typed_rel_concrete() throws Exception {
        Plan plan = create_Typ_Rel_Con_PathQuery();
        Ontology.Accessor ont = getOntologyAccessor();
        Traversal actualTraversal = translator.translate(new PlanWithCost<>(plan, null), new TranslationContext(ont, new PromiseGraph().traversal()));

/*
        Traversal expectedTraversal =
                new PromiseGraph().traversal().V().as("B")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().has(T.label, "Dragon")))
                        .outE().as("B-->A")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().has(T.label, "Fire")))
                        .otherV().as("A");
*/

        Assert.assertEquals("[GraphStep(vertex,[])@[B], HasStep([constraint.eq(Constraint.by([HasStep([~label.eq(Dragon)])]))]), VertexStep(OUT,edge)@[B-->A], HasStep([constraint.eq(Constraint.by([HasStep([~label.eq(Fire)])]))]), EdgeOtherVertexStep, VertexStep(OUT,[promiseFilter],edge), HasStep([constraint.eq(Constraint.by([HasStep([~id.eq(12345678)])]))]), EdgeOtherVertexStep@[A]]",
                actualTraversal.toString());
    }

    @Test
    public void test_typed_rel_typed() throws Exception {
        Plan plan = create_Typ_Rel_Typ_PathQuery();
        Ontology.Accessor ont = getOntologyAccessor();
        Traversal actualTraversal = translator.translate(new PlanWithCost<>(plan, null), new TranslationContext(ont, new PromiseGraph().traversal()));

        Traversal expectedTraversal =
                new PromiseGraph().traversal().V().as("A")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().has(T.label, "Person")))
                        .outE().as("A-->B")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().has(T.label, "Fire")))
                        .otherV().as("B");

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void test_concrete_rel_typed_rel_untyped() throws Exception {
        Plan plan = create_Con_Rel_Typ_Rel_Unt_PathQuery();
        Ontology.Accessor ont = getOntologyAccessor();
        Traversal actualTraversal = translator.translate(new PlanWithCost<>(plan, null), new TranslationContext(ont, new PromiseGraph().traversal()));

        Traversal expectedTraversal =
                new PromiseGraph().traversal().V().as("A")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().and(
                                __.start().has(T.id, "12345678"),
                                __.start().has(T.label, "Person"))))
                        .outE().as("A-->B")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().has(T.label, "Fire")))
                        .otherV().as("B")
                        .outE().as("B-->C")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().has(T.label, "Fire")))
                        .otherV().as("C");

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    //region Building Plans
    private Plan create_Typ_Rel_Con_PathQuery() {
        AsgQuery query = AsgQuery.Builder.start("name", "ont")
                .next(typed(1, "2", "B"))
                .next(quant1(2, QuantType.all))
                .in(ePropGroup(3))
                .next(rel(4, "1", Rel.Direction.R).below(relProp(5)))
                .next(concrete(6, "12345678", "1", "Dardas Aba", "A"))
                .next(quant1(7, QuantType.all))
                .next(ePropGroup(8))
                .build();

        return new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 1)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 3)),
                new RelationOp(AsgQueryUtil.element$(query, 4)),
                new RelationFilterOp(AsgQueryUtil.element$(query, 5)),
                new EntityOp(AsgQueryUtil.element$(query, 6)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 8))
        );
    }

    private Plan create_Typ_Rel_Typ_PathQuery() {
        AsgQuery query = AsgQuery.Builder.start("name", "ont")
                .next(typed(1, "1", "A"))
                .next(quant1(2, QuantType.all))
                .in(ePropGroup(3))
                .next(rel(4, "1", Rel.Direction.R).below(relProp(5)))
                .next(typed(6, "2", "B"))
                .next(quant1(7, QuantType.all))
                .next(ePropGroup(8))
                .build();

        return new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 1)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 3)),
                new RelationOp(AsgQueryUtil.element$(query, 4)),
                new RelationFilterOp(AsgQueryUtil.element$(query, 5)),
                new EntityOp(AsgQueryUtil.element$(query, 6)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 8))
        );
    }

    private Plan create_Con_Rel_Typ_PathQuery() {
        AsgQuery query = AsgQuery.Builder.start("name", "ont")
                .next(concrete(1, "12345678", "1", "Dardas Aba", "A"))
                .next(quant1(2, QuantType.all))
                .in(ePropGroup(3))
                .next(rel(4, "1", Rel.Direction.R).below(relProp(5)))
                .next(typed(6, "1", "B"))
                .next(quant1(7, QuantType.all))
                .next(ePropGroup(8))
                .build();

        return new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 1)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 3)),
                new RelationOp(AsgQueryUtil.element$(query, 4)),
                new RelationFilterOp(AsgQueryUtil.element$(query, 5)),
                new EntityOp(AsgQueryUtil.element$(query, 6)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 8))
        );
    }

    private Plan create_Con_Rel_Typ_Agg_PathQuery() {
        AsgQuery query = AsgQuery.Builder.start("name", "ont")
                .next(concrete(1, "12345678", "1", "Dardas Aba", "A"))
                .next(agg(2,org.opensearch.graph.model.query.properties.constraint.Constraint.of(CountConstraintOp.le,100),"groupBy"))
                .next(quant1(3, QuantType.all))
                .in(ePropGroup(4))
                .next(rel(5, "1", Rel.Direction.R).below(relProp(6)))
                .next(typed(7, "1", "B"))
                .next(quant1(8, QuantType.all))
                .next(ePropGroup(9))
                .build();

        return new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 1)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 3)),
                new RelationOp(AsgQueryUtil.element$(query, 4)),
                new RelationFilterOp(AsgQueryUtil.element$(query, 5)),
                new EntityOp(AsgQueryUtil.element$(query, 6)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 8))
        );
    }

    private Plan create_Con_Rel_Con_PathQuery() {
        AsgQuery query = AsgQuery.Builder.start("name", "ont")
                .next(concrete(1, "12345678", "1", "Dardas Aba", "A"))
                .next(quant1(2, QuantType.all))
                .in(ePropGroup(3))
                .next(rel(4, "1", Rel.Direction.R).below(relProp(5)))
                .next(concrete(6, "87654321","1","Baba Buba", "B"))
                .build();

        return new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 1)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 3)),
                new RelationOp(AsgQueryUtil.element$(query, 4)),
                new RelationFilterOp(AsgQueryUtil.element$(query, 5)),
                new EntityOp(AsgQueryUtil.element$(query, 6)));
    }

    private Plan create_Con_Rel_Unt_PathQuery() {
        AsgQuery query = AsgQuery.Builder.start("name", "ont")
                .next(concrete(1, "12345678", "1", "Dardas Aba", "A"))
                .next(quant1(2, QuantType.all))
                .in(ePropGroup(3))
                .next(rel(4, "1", Rel.Direction.R).below(relProp(5)))
                .next(unTyped(6, "B"))
                .next(quant1(7, QuantType.all))
                .next(ePropGroup(8))
                .build();

        return new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 1)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 3)),
                new RelationOp(AsgQueryUtil.element$(query, 4)),
                new RelationFilterOp(AsgQueryUtil.element$(query, 5)),
                new EntityOp(AsgQueryUtil.element$(query, 6)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 8))
        );
    }

    private Plan create_Con_Rel_Typ_Rel_Unt_PathQuery()
    {
        AsgQuery query = AsgQuery.Builder.start("name", "ont")
                .next(concrete(1, "12345678", "1", "Dardas Aba", "A"))
                .next(quant1(2, QuantType.all))
                .in(ePropGroup(3))
                .next(rel(4, "1", Rel.Direction.R).below(relProp(5)))
                .next(typed(6, "2", "B"))
                .next(quant1(7, QuantType.all))
                .in(ePropGroup(8))
                .next(rel(9, "1", Rel.Direction.R).below(relProp(10)))
                .next(unTyped(11, "C"))
                .next(quant1(12, QuantType.all))
                .next(ePropGroup(13))
                .build();

        return new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 1)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 3)),
                new RelationOp(AsgQueryUtil.element$(query, 4)),
                new RelationFilterOp(AsgQueryUtil.element$(query, 5)),
                new EntityOp(AsgQueryUtil.element$(query, 6)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 8)),
                new RelationOp(AsgQueryUtil.element$(query, 9)),
                new RelationFilterOp(AsgQueryUtil.element$(query, 10)),
                new EntityOp(AsgQueryUtil.element$(query, 11)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 13))
        );
    }
    //endregion


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

        return new Ontology.Accessor(ontology);
    }
}
