package org.opensearch.graph.gta.strategy.promise;

import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.gta.strategy.PlanOpTranslationStrategy;
import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.model.OntologyTestUtils.DRAGON;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.*;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.projection.IdentityProjection;
import org.opensearch.graph.model.query.quant.QuantType;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.unipop.predicates.SelectP;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import static org.opensearch.graph.model.OntologyTestUtils.*;
import static org.opensearch.graph.model.OntologyTestUtils.FIRE;
import static org.opensearch.graph.model.OntologyTestUtils.FIRST_NAME;
import static org.opensearch.graph.model.OntologyTestUtils.LAST_NAME;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.query.Rel.Direction.R;
import static org.mockito.Mockito.when;

/**
 * Created by Roman on 28/05/2017.
 */
public class EntitySelectionTranslationStrategyTest {
    //region Setup
    @BeforeClass
    public static void setup() {
        ont = new Ontology.Accessor(createDragonsOntologyLong());
    }
    //endregion

    //region Test Methods
    @Test
    public void test_selection_entity1_filter5() {
        AsgQuery query = simpleQuery1("name", "ont");

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 1)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 5))
        );

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOnt()).thenAnswer(invocationOnMock -> ont);

        PlanOpTranslationStrategy strategy = new EntitySelectionTranslationStrategy();
        GraphTraversal actualTraversal = strategy.translate(__.start(), new PlanWithCost<>(plan, null), plan.getOps().get(1), context);

        GraphTraversal expectedTraversal = __.start()
                .has(FIRST_NAME.name, SelectP.raw(FIRST_NAME.name))
                .has(LAST_NAME.name, SelectP.raw(LAST_NAME.name));

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void test_selection_entity1_filter5_distinct_select() {
        AsgQuery query = simpleQuery1("name", "ont");

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 1)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 5))
        );

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOnt()).thenAnswer(invocationOnMock -> ont);

        PlanOpTranslationStrategy strategy = new EntitySelectionTranslationStrategy();
        GraphTraversal actualTraversal = strategy.translate(
                __.start().has(FIRST_NAME.name, SelectP.raw(FIRST_NAME.name)), new PlanWithCost<>(plan, null), plan.getOps().get(1), context);

        GraphTraversal expectedTraversal = __.start()
                .has(FIRST_NAME.name, SelectP.raw(FIRST_NAME.name))
                .has(LAST_NAME.name, SelectP.raw(LAST_NAME.name));

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void test_selection_filter5() {
        AsgQuery query = simpleQuery1("name", "ont");

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 1)),
                new RelationOp(AsgQueryUtil.element$(query, 2)),
                new EntityOp(AsgQueryUtil.element$(query, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 5)));

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOnt()).thenAnswer(invocationOnMock -> ont);

        PlanOpTranslationStrategy strategy = new EntitySelectionTranslationStrategy();
        GraphTraversal actualTraversal = strategy.translate(
                __.start().outE(GlobalConstants.Labels.PROMISE_FILTER), new PlanWithCost<>(plan, null), plan.getOps().get(3), context);

        GraphTraversal expectedTraversal = __.start()
                .outE(GlobalConstants.Labels.PROMISE_FILTER)
                .has(FIRST_NAME.name, SelectP.raw(FIRST_NAME.name))
                .has(LAST_NAME.name, SelectP.raw(LAST_NAME.name))
                .otherV().as("B");

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }
    //endregion

    //region Private Methods
    private static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, PERSON.type,"A"))
                .next(quant1(4, QuantType.all))
                .in(ePropGroup(5, new EProp(5, LAST_NAME.type, Constraint.of(ConstraintOp.eq, "last")),
                            new EProp(5, FIRST_NAME.type, new IdentityProjection()),
                            new EProp(5, LAST_NAME.type, new IdentityProjection())))
                .next(rel(2,FIRE.getrType(),R))
                .next(typed(3, DRAGON.type,"B"))
                .next(quant1(6, QuantType.all))
                .next(ePropGroup(7, new EProp(7, FIRST_NAME.type, Constraint.of(ConstraintOp.eq, "name")),
                        new EProp(7, FIRST_NAME.type, new IdentityProjection()),
                        new EProp(7, LAST_NAME.type, new IdentityProjection())))
                .build();
    }
    //endregion

    //region Fields
    private static Ontology.Accessor ont;
    //endregion
}
