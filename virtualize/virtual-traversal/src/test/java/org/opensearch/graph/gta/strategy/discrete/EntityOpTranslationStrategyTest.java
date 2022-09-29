package org.opensearch.graph.gta.strategy.discrete;

import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.gta.strategy.common.EntityTranslationOptions;
import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.*;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationFilterOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.ontology.EntityType;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.Start;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.RelProp;
import org.opensearch.graph.model.query.properties.RelPropGroup;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.unipop.process.traversal.dsl.graph.SearchGraphTraversalSource;
import org.opensearch.graph.unipop.promise.Constraint;
import org.opensearch.graph.unipop.structure.SearchUniGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.opensearch.graph.unipop.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.util.DefaultTraversalStrategies;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.unipop.query.controller.ControllerManager;
import org.unipop.query.controller.UniQueryController;

import java.util.Collections;
import java.util.Set;

import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.query.properties.constraint.Constraint.of;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.eq;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.gt;
import static org.opensearch.graph.model.query.Rel.Direction.R;
import static org.opensearch.graph.model.query.quant.QuantType.all;
import static org.mockito.Mockito.when;

/**
 * Created by roman.margolis on 26/09/2017.
 */
public class EntityOpTranslationStrategyTest {
    public static AsgQuery simpleQuery0(String queryName, String ontologyName) {
        Start start = new Start();
        start.seteNum(0);

        ETyped eTyped = new ETyped();
        eTyped.seteNum(1);
        eTyped.seteTag("A");
        eTyped.seteType("1");

        AsgEBase<Start> asgStart =
                AsgEBase.Builder.<Start>get().withEBase(start)
                        .withNext(AsgEBase.Builder.get().withEBase(eTyped).build())
                        .build();

        return AsgQuery.AsgQueryBuilder.anAsgQuery().withName(queryName).withOnt(ontologyName).withStart(asgStart).build();
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

    public static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, "1", "A"))
                .next(rel(2, "1", R).below(relProp(10, RelProp.of(10, "2", of(eq, "value2")))))
                .next(typed(3, "2", "B"))
                .next(quant1(4, all))
                .in(ePropGroup(9, EProp.of(9, "1", of(eq, "value1")), EProp.of(9, "3", of(gt, "value3")))
                        , rel(5, "4", R)
                                .next(unTyped(6, "C"))
                        , rel(7, "5", R)
                                .below(relProp(11, RelProp.of(11, "5", of(eq, "value5")), RelProp.of(11, "4", of(eq, "value4"))))
                                .next(concrete(8, "concrete1", "3", "Concrete1", "D"))
                )
                .build();
    }

    public static AsgQuery simpleQuery3(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, "1", "A"))
                .next(rel(2, "1", R).below(relProp(10, RelProp.of(10, "2", of(eq, "value2")))))
                .next(concrete(3, "123456","2","concrete", "B"))
                .next(quant1(4, all))
                .in(ePropGroup(9, EProp.of(9, "1", of(eq, "value1")), EProp.of(9, "3", of(gt, "value3")))
                        , rel(5, "4", R)
                                .next(unTyped(6, "C"))
                        , rel(7, "5", R)
                                .below(relProp(11, RelProp.of(11, "5", of(eq, "value5")), RelProp.of(11, "4", of(eq, "value4"))))
                                .next(concrete(8, "concrete1", "3", "Concrete1", "D"))
                )
                .build();
    }

    @Test
    public void testOptions_none_entity1() throws Exception {
        AsgQuery query = simpleQuery0("name", "ontName");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(query, 1).get())
        );

        EntityOpTranslationStrategy strategy = new EntityOpTranslationStrategy(EntityTranslationOptions.none);

        Ontology ontology = Ontology.OntologyBuilder.anOntology("ont").withEntityTypes(Collections.singletonList(
                EntityType.Builder.get().withEType("1").withName("Person").build()
        )).build();

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOnt()).thenReturn(new Ontology.Accessor(ontology));
        when(context.getGraphTraversalSource()).thenReturn(new SearchGraphTraversalSource(new SearchUniGraph(null, graph -> new ControllerManager() {
            @Override
            public Set<UniQueryController> getControllers() {
                return Collections.emptySet();
            }

            @Override
            public void close() {

            }
        }, DefaultTraversalStrategies::new)));

        GraphTraversal actualTraversal = strategy.translate(__.start(), new PlanWithCost<>(plan, null), plan.getOps().get(0), context);
        GraphTraversal expectedTraversal = __.start().V().as("A")
                .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().has(T.label, "Person")));

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void testOptions_none_entity1_rel2_entity3() throws Exception {
        AsgQuery query = simpleQuery1("name", "ontName");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(query, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(query, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(query, 3).get())
        );
        Ontology ontology = Ontology.OntologyBuilder.anOntology("ont").withEntityTypes(Collections.singletonList(
                EntityType.Builder.get().withEType("2").withName("Person").build()
        )).build();

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOnt()).thenReturn(new Ontology.Accessor(ontology));

        EntityOpTranslationStrategy strategy = new EntityOpTranslationStrategy(EntityTranslationOptions.none);

        GraphTraversal actualTraversal = strategy.translate(__.start(), new PlanWithCost<>(plan, null), plan.getOps().get(2), context);
        String expectedTraversal = "[EdgeOtherVertexStep@[B], HasStep([constraint.eq(Constraint.by([HasStep([~label.eq(Person)])]))])]";

        Assert.assertEquals(expectedTraversal, actualTraversal.toString());
    }

    @Test
    public void testOptions_filterEntity_entity1_rel2_entity3() throws Exception {
        AsgQuery query = simpleQuery1("name", "ontName");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(query, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(query, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(query, 3).get())
        );

        Ontology ontology = Ontology.OntologyBuilder.anOntology("ont").withEntityTypes(Collections.singletonList(
                EntityType.Builder.get().withEType("2").withName("Person").build()
        )).build();

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOnt()).thenReturn(new Ontology.Accessor(ontology));

        EntityOpTranslationStrategy strategy = new EntityOpTranslationStrategy(EntityTranslationOptions.filterEntity);
        GraphTraversal actualTraversal = strategy.translate(__.start(), new PlanWithCost<>(plan, null), plan.getOps().get(2), context);
        GraphTraversal expectedTraversal = __.start().otherV()
                .outE(GlobalConstants.Labels.PROMISE_FILTER)
                .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().has(T.label, "Person")))
                .otherV().as("B");

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void testOptions_none_entity1_rel2_filter10_entity3() throws Exception {
        AsgQuery query = simpleQuery2("name", "ontName");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(query, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(query, 2).get()),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(query, 10).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(query, 3).get())
        );


        Ontology ontology = Ontology.OntologyBuilder.anOntology("ont").withEntityTypes(Collections.singletonList(
                EntityType.Builder.get().withEType("2").withName("Person").build()
        )).build();

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOnt()).thenReturn(new Ontology.Accessor(ontology));


        EntityOpTranslationStrategy strategy = new EntityOpTranslationStrategy(EntityTranslationOptions.none);

        GraphTraversal actualTraversal = strategy.translate(__.start(), new PlanWithCost<>(plan, null), plan.getOps().get(3), context);
        String expectedTraversal = "[EdgeOtherVertexStep@[B], HasStep([constraint.eq(Constraint.by([HasStep([~label.eq(Person)])]))])]";

        Assert.assertEquals(expectedTraversal, actualTraversal.toString());
    }

    @Test
    public void testOptions_none_entity1_concrete_rel2_filter10_entity3() throws Exception {
        AsgQuery query = simpleQuery3("name", "ontName");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(query, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(query, 2).get()),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(query, 10).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(query, 3).get())
        );


        Ontology ontology = Ontology.OntologyBuilder.anOntology("ont").withEntityTypes(Collections.singletonList(
                EntityType.Builder.get().withEType("2").withName("Person").build()
        )).build();

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOnt()).thenReturn(new Ontology.Accessor(ontology));


        EntityOpTranslationStrategy strategy = new EntityOpTranslationStrategy(EntityTranslationOptions.none);

        GraphTraversal actualTraversal = strategy.translate(__.start(), new PlanWithCost<>(plan, null), plan.getOps().get(3), context);
        String expected = "[EdgeOtherVertexStep@[B], HasStep([constraint.eq(Constraint.by([AndStep([[HasStep([~id.eq(123456)])], [HasStep([~label.eq(Person)])]])]))])]";

        Assert.assertEquals(expected, actualTraversal.toString());
    }
}