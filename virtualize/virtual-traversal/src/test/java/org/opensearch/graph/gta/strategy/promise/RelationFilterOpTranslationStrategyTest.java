package org.opensearch.graph.gta.strategy.promise;

import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.*;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.relation.RelationFilterOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.Property;
import org.opensearch.graph.model.ontology.RelationshipType;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.RelProp;
import org.opensearch.graph.model.query.properties.RelPropGroup;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.unipop.promise.Constraint;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;

import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.query.properties.constraint.Constraint.of;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.eq;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.gt;
import static org.opensearch.graph.model.query.Rel.Direction.R;
import static org.opensearch.graph.model.query.quant.QuantType.all;
import static org.mockito.Mockito.when;

/**
 * Created by Roman on 10/05/2017.
 */
public class RelationFilterOpTranslationStrategyTest {
    static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, "1"))
                .next(rel(2, "1", R).below(relProp(10, RelProp.of(10, "2", of(gt, 10)))))
                .next(typed(3, "2"))
                .next(quant1(4, all))
                .in(ePropGroup(9, EProp.of(9, "1", of(eq, "value1")), EProp.of(9, "2", of(eq, 30)))
                        , rel(5, "4", R)
                                .next(unTyped(6, "C"))
                        , rel(7, "5", R)
                                .below(relProp(11, RelProp.of(11, "5", of(eq, "value5")), RelProp.of(11, "4", of(eq, "value4"))))
                                .next(concrete(8, "concrete1", "3", "Concrete1", "D"))
                )
                .build();
    }

    @Test
    public void test_rel2_filter10() {
        AsgQuery query = simpleQuery2("name", "ontName");
        Plan plan = new Plan(
                new RelationOp(AsgQueryUtil.<Rel>element(query, 2).get()),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(query, 10).get())
        );

        Ontology ontology = Mockito.mock(Ontology.class);
        when(ontology.getRelationshipTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<RelationshipType> relTypes = new ArrayList<>();
                    relTypes.add(RelationshipType.Builder.get()
                            .withRType("1").withName("Fire").build());
                    return  relTypes;
                }
        );

        when(ontology.getProperties()).then(invocationOnMock -> {
            Property timestampProperty = new Property();
            timestampProperty.setpType("2");
            timestampProperty.setName("timestamp");
            timestampProperty.setType("int");

            return Collections.singleton(timestampProperty);
        });

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOnt()).thenAnswer(invocationOnMock -> new Ontology.Accessor(ontology));

        RelationFilterOpTranslationStrategy strategy = new RelationFilterOpTranslationStrategy();
        GraphTraversal actualTraversal = strategy.translate(
                __.start().has("willBeDeleted", "doesnt matter"),
                new PlanWithCost<>(plan, null),
                plan.getOps().get(1),
                context);

        GraphTraversal expectedTraversal = __.start()
                .has(GlobalConstants.HasKeys.CONSTRAINT,
                        Constraint.by(__.and(
                            __.has(T.label, "Fire"),
                            __.has(GlobalConstants.HasKeys.DIRECTION, Direction.OUT),
                            __.has("timestamp", P.gt(10)))));

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void test_reversed_rel2_filter10() {
        AsgQuery query = simpleQuery2("name", "ontName");
        Plan plan = new Plan(
                new RelationOp(AsgQueryUtil.reverse(AsgQueryUtil.<Rel>element(query, 2).get())),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(query, 10).get())
        );

        Ontology ontology = Mockito.mock(Ontology.class);
        when(ontology.getRelationshipTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<RelationshipType> relTypes = new ArrayList<>();
                    relTypes.add(RelationshipType.Builder.get()
                            .withRType("1").withName("Fire").build());
                    return  relTypes;
                }
        );

        when(ontology.getProperties()).then(invocationOnMock -> {
            Property timestampProperty = new Property();
            timestampProperty.setpType("2");
            timestampProperty.setName("timestamp");
            timestampProperty.setType("int");

            return Collections.singleton(timestampProperty);
        });

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOnt()).thenAnswer(invocationOnMock -> new Ontology.Accessor(ontology));

        RelationFilterOpTranslationStrategy strategy = new RelationFilterOpTranslationStrategy();
        GraphTraversal actualTraversal = strategy.translate(
                __.start().has("willBeDeleted", "doesnt matter"),
                new PlanWithCost<>(plan, null),
                plan.getOps().get(1),
                context);

        GraphTraversal expectedTraversal = __.start()
                .has(GlobalConstants.HasKeys.CONSTRAINT,
                        Constraint.by(__.and(
                                __.has(T.label, "Fire"),
                                __.has(GlobalConstants.HasKeys.DIRECTION, Direction.IN),
                                __.has("timestamp", P.gt(10)))));

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }
}
