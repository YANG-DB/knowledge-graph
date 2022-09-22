package org.opensearch.graph.epb.plan.extenders;

import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.dispatcher.utils.PlanUtil;
import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.model.OntologyTestUtils.DRAGON;
import org.opensearch.graph.model.OntologyTestUtils.PERSON;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanAssert;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationFilterOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.properties.*;
import org.opensearch.graph.model.query.quant.QuantType;
import org.opensearch.graph.model.schema.BaseTypeElement;
import org.opensearch.graph.unipop.schemaProviders.*;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.opensearch.graph.model.OntologyTestUtils.OWN;
import static org.opensearch.graph.model.OntologyTestUtils.START_DATE;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.query.properties.constraint.Constraint.of;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.eq;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.gt;
import static org.opensearch.graph.model.query.Rel.Direction.R;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.inSet;
import static org.opensearch.graph.model.query.quant.QuantType.all;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opensearch.graph.model.schema.BaseTypeElement.*;

public class RedundantFilterPlanExtensionStrategyTest {
    @Before
    public void setup(){
        this.ontologyProvider = mock(OntologyProvider.class);
        when(ontologyProvider.get(any())).thenReturn(Optional.of(OntologyTestUtils.createDragonsOntologyShort()));

        this.schemaProviderFactory = ontology -> buildSchemaProvider(new Ontology.Accessor(ontology));
    }

    //region Test Methods
    @Test
    public void test_EConcreteRedundantFilterSplitPlan() {
        AsgQuery asgQuery = AsgQuery.Builder.start("name", "ont" )
                .next(typed(1, PERSON.type))
                .next(rel(2, OWN.getrType(), R)
                        .below(relProp(10, RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(concrete(3, "123", DRAGON.type, "B", "tag"))
                .build();

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)));

        RedundantFilterPlanExtensionStrategy strategy = new RedundantFilterPlanExtensionStrategy(this.ontologyProvider, this.schemaProviderFactory);

        List<Plan> extendedPlans = Stream.ofAll(strategy.extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 1);
        assertEquals(PlanUtil.first$(extendedPlans.get(0), RelationFilterOp.class).getAsgEbase().geteBase().getProps().size(),3);
        //first ePropGroup is the old eprop filter condition (non pushdown)
        assertTrue(PlanUtil.first$(extendedPlans.get(0), RelationFilterOp.class).getAsgEbase().geteBase().getProps().get(1) instanceof RedundantRelProp);
        Optional<RelProp> idRelProp = PlanUtil.first$(extendedPlans.get(0), RelationFilterOp.class).getAsgEbase().geteBase().getProps().stream().
                filter(r -> r instanceof RedundantRelProp && ((RedundantRelProp) r).getRedundantPropName().equals(GlobalConstants.EdgeSchema.DEST_ID)).findFirst();
        Assert.assertTrue(idRelProp.isPresent());
        Assert.assertEquals("123",idRelProp.get().getCon().getExpr());
    }

    @Test
    public void test_simpleQuery2RedundantFilterSplitPlan() {
        AsgQuery asgQuery = AsgQuery.Builder.start("name", "ont")
                .next(typed(1,  PERSON.type))
                .next(rel(2, OWN.getrType(), R)
                        .below(relProp(10, RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(typed(3,  DRAGON.type))
                .next(quant1(4, all))
                .in(ePropGroup(9, EProp.of(9, "firstName", of(eq, "value1")), EProp.of(9, "gender", of(gt, "value3")))
                        , rel(5, "4", R)
                                .next(unTyped( 6))
                        , rel(7, "5", R)
                                .below(relProp(11, RelProp.of(11, "deathDate", of(eq, "value5")), RelProp.of(11, "birthDate", of(eq, "value4"))))
                                .next(concrete(8, "concrete1", "3", "Concrete1", "D"))
                )
                .build();

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)));

        RedundantFilterPlanExtensionStrategy strategy = new RedundantFilterPlanExtensionStrategy(this.ontologyProvider, this.schemaProviderFactory);

        List<Plan> extendedPlans = Stream.ofAll(strategy.extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 1);

        assertEquals(0,PlanUtil.first$(extendedPlans.get(0), EntityFilterOp.class).getAsgEbase().geteBase().getProps().size());
        assertEquals(4,PlanUtil.first$(extendedPlans.get(0), RelationFilterOp.class).getAsgEbase().geteBase().getProps().size());

        //first ePropGroup is the old eprop filter condition (non pushdown)
        assertTrue(PlanUtil.first$(extendedPlans.get(0), RelationFilterOp.class).getAsgEbase().geteBase().getProps().get(1) instanceof RedundantRelProp);
        assertTrue(PlanUtil.first$(extendedPlans.get(0), RelationFilterOp.class).getAsgEbase().geteBase().getProps().get(2) instanceof RedundantRelProp);

        Optional<RelProp> firstNameRelProp = PlanUtil.first$(extendedPlans.get(0), RelationFilterOp.class).getAsgEbase().geteBase().getProps().stream().
                filter(r -> r instanceof RedundantRelProp && ((RedundantRelProp) r).getRedundantPropName().equals("entityB.firstName")).findFirst();
        Assert.assertTrue(firstNameRelProp.isPresent());
        Optional<RelProp> typeRelProp = PlanUtil.first$(extendedPlans.get(0), RelationFilterOp.class).getAsgEbase().geteBase().getProps().stream().
                filter(r -> r instanceof RedundantRelProp && ((RedundantRelProp) r).getRedundantPropName().equals(GlobalConstants.EdgeSchema.DEST_TYPE)).findFirst();
        Assert.assertTrue(typeRelProp.isPresent());
        Assert.assertEquals("Dragon",((List<String>)typeRelProp.get().getCon().getExpr()).get(0));
    }

    @Test
    public void test_simpleQuery3() {
        AsgQuery asgQuery = AsgQuery.Builder.start("name", "ont")
                .next(typed(1, PERSON.type))
                .next(rel(2, OWN.getrType(), R)
                        .below(relProp(10, RelProp.of(0, START_DATE.type, of(eq, new Date(1000))))))
                .next(typed(3, DRAGON.type))
                .next(quant1(4, all))
                .in(ePropGroup(9, QuantType.all,
                        Stream.of(EProp.of(0, "name", of(eq, "value1"))),
                        Stream.of(EPropGroup.of(0, QuantType.some,
                                EPropGroup.of(0, EProp.of(0, "gender", of(eq, "male")), EProp.of(0, "color", of(eq, "red"))),
                                EPropGroup.of(0, EProp.of(0, "gender", of(eq, "female")), EProp.of(0, "color", of(eq, "blue")))))))
                .build();

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)));

        RedundantFilterPlanExtensionStrategy strategy = new RedundantFilterPlanExtensionStrategy(this.ontologyProvider, this.schemaProviderFactory);

        List<Plan> extendedPlans = Stream.ofAll(strategy.extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 1);

        Plan actualPlan = extendedPlans.get(0);

        Plan expectedPlan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgEBase.Builder.<RelPropGroup>get().withEBase(
                        RelPropGroup.of(10, QuantType.all,
                                Stream.of(
                                        RelProp.of(0, START_DATE.type, of(eq, new Date(1000))),
                                        RedundantRelProp.of(0, GlobalConstants.EdgeSchema.DEST_TYPE, "type", of(inSet, Collections.singletonList("Dragon"))),
                                        RedundantRelProp.of(0, GlobalConstants.EdgeSchema.DEST_NAME, "name", of(eq, "value1"))),
                                Stream.of(RelPropGroup.of(0, QuantType.some,
                                        RelPropGroup.of(0, RedundantRelProp.of(0, "entityB.gender", "gender", of(eq, "male"))),
                                        RelPropGroup.of(0, RedundantRelProp.of(0, "entityB.gender", "gender", of(eq, "female")))))))
                        .build()),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgEBase.Builder.<EPropGroup>get().withEBase(
                        EPropGroup.of(9, QuantType.all,
                                EPropGroup.of(0, QuantType.some,
                                        EPropGroup.of(0, EProp.of(0, "gender", of(eq, "male")), EProp.of(0, "color", of(eq, "red"))),
                                        EPropGroup.of(0, EProp.of(0, "gender", of(eq, "female")), EProp.of(0, "color", of(eq, "blue"))))))
                        .build()));

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }
    //endregion

    //region Private Methods
    private GraphElementSchemaProvider buildSchemaProvider(Ontology.Accessor ont) {
        Iterable<GraphVertexSchema> vertexSchemas =
                Stream.ofAll(ont.entities())
                        .map(entity -> (GraphVertexSchema) new GraphVertexSchema.Impl(
                                Type.of(entity.geteType()),
                                new StaticIndexPartitions(Collections.singletonList("index"))))
                        .toJavaList();

        Iterable<GraphEdgeSchema> edgeSchemas =
                Stream.ofAll(ont.relations())
                        .map(relation -> (GraphEdgeSchema) new GraphEdgeSchema.Impl(
                                Type.of(relation.getrType()),
                                new GraphElementConstraint.Impl(__.has(T.label, relation.getrType())),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(GlobalConstants.EdgeSchema.SOURCE_ID),
                                        Optional.of(relation.getePairs().get(0).geteTypeA()))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(GlobalConstants.EdgeSchema.DEST_ID),
                                        Optional.of(relation.getePairs().get(0).geteTypeB()),
                                        Arrays.asList(
                                                new GraphRedundantPropertySchema.Impl("firstName", "entityB.firstName", ont.property$("firstName").getType()),
                                                new GraphRedundantPropertySchema.Impl("name", GlobalConstants.EdgeSchema.DEST_NAME, ont.property$("name").getType()),
                                                new GraphRedundantPropertySchema.Impl("gender", "entityB.gender", ont.property$("gender").getType()),
                                                new GraphRedundantPropertySchema.Impl("id", GlobalConstants.EdgeSchema.DEST_ID, ont.property$("firstName").getType()),
                                                new GraphRedundantPropertySchema.Impl("type", GlobalConstants.EdgeSchema.DEST_TYPE, ont.property$("type").getType())
                                        ))),
                                Direction.OUT,
                                Optional.of(new GraphEdgeSchema.DirectionSchema.Impl("direction", "out", "in")),
                                Optional.empty(),
                                Optional.of(new StaticIndexPartitions(Collections.singletonList("index"))),
                                Collections.emptyList()))
                        .toJavaList();

        return new OntologySchemaProvider(ont.get(), new GraphElementSchemaProvider.Impl(vertexSchemas, edgeSchemas));
    }
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    //endregion
}
