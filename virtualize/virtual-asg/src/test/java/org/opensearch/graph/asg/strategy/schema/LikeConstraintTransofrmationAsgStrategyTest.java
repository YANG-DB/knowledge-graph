package org.opensearch.graph.asg.strategy.schema;

import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.EntityType;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.Property;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.SchematicEProp;
import org.opensearch.graph.unipop.schemaProviders.GraphElementConstraint;
import org.opensearch.graph.unipop.schemaProviders.GraphElementPropertySchema;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;
import org.opensearch.graph.unipop.schemaProviders.GraphVertexSchema;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensearch.graph.asg.strategy.AsgStrategy;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.query.quant.QuantType.all;

/**
 * Created by roman.margolis on 05/02/2018.
 */
public class LikeConstraintTransofrmationAsgStrategyTest {
    //region Setup
    @BeforeClass
    public static void setup() {
        Ontology ontology = Ontology.OntologyBuilder.anOntology()
                .withEntityTypes(Collections.singletonList(
                        EntityType.Builder.get().withEType("Person").withName("Person").withProperties(
                                Collections.singletonList("name")).build()))
                .withProperties(Collections.singleton(
                        Property.Builder.get().withPType("name").withName("name").withType("string").build()))
                .build();

        OntologyProvider ontologyProvider = new OntologyProvider() {
            @Override
            public Optional<Ontology> get(String id) {
                return Optional.of(ontology);
            }

            @Override
            public Collection<Ontology> getAll() {
                return Collections.singleton(ontology);
            }

            @Override
            public Ontology add(Ontology ontology) {
                return ontology;
             }
        };

        GraphElementSchemaProvider schemaProvider = new GraphElementSchemaProvider.Impl(
                Collections.singletonList(
                        new GraphVertexSchema.Impl(
                                "Person",
                                new GraphElementConstraint.Impl(__.start()),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.singletonList(
                                        new GraphElementPropertySchema.Impl("name", "string", Arrays.asList(
                                                new GraphElementPropertySchema.ExactIndexingSchema.Impl("name.keyword"),
                                                new GraphElementPropertySchema.NgramsIndexingSchema.Impl("name.ngrams", 10)
                                        ))
                                )
                        )
                ),
                Collections.emptyList());

        GraphElementSchemaProviderFactory schemaProviderFactory = ontology1 -> schemaProvider;

        asgStrategy = new LikeConstraintTransformationAsgStrategy(ontologyProvider, schemaProviderFactory);
        context = new AsgStrategyContext(new Ontology.Accessor(ontology));
    }
    //endregion

    //region Tests
    @Test
    public void testLikeWithoutWildcards() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "Sherley"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3, Collections.singletonList(
                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.eq, "Sherley"))));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeWith1Wildcard_Beginning() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "*Sherley"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3, Arrays.asList(
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "Sherley")),
                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "*Sherley"))));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeWith1Wildcard_Middle() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "She*rley"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3, Arrays.asList(
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "She")),
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "rley")),
                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "She*")),
                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "*rley"))));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeWith1Wildcard_Ending() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "Sherley*"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3, Arrays.asList(
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "Sherley")),
                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "Sherley*"))));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeWith2Wildcards_BeginningMiddle() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "*She*rley"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3, Arrays.asList(
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "rley")),
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "She")),
                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "*rley"))));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeWith2Wildcards_BeginningEnding() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "*Sherley*"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3, Collections.singletonList(
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "Sherley"))));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeWith2Wildcards_MiddleEnding() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "She*rley*"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3, Arrays.asList(
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "She")),
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "rley")),
                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "She*"))));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeWith2ConsecutiveWildcards_Beginning() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "**Sherley"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3, Arrays.asList(
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "Sherley")),
                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "*Sherley"))));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeWith2ConsecutiveWildcards_Middle() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "She**rley"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3, Arrays.asList(
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "She")),
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "rley")),
                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "She*")),
                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "*rley"))));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeWith2ConsecutiveWildcards_Ending() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "Sherley**"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3, Arrays.asList(
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "Sherley")),
                new SchematicEProp(0, "name", "name.keyword", Constraint.of(ConstraintOp.like, "Sherley*"))));

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLikeWith2ConsecutiveWildcardsOnly() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "**"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();
        Assert.assertTrue(actual.getProps().isEmpty());
    }

    @Test
    public void testLikeWith3Wildcards_BeginningMiddleEnding() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "*She*rley*"))))
                .build();

        asgStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();

        EPropGroup expected = new EPropGroup(3, Arrays.asList(
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "She")),
                new SchematicEProp(0, "name", "name.ngrams", Constraint.of(ConstraintOp.eq, "rley"))));

        Assert.assertEquals(expected, actual);
    }
    //endregion

    //region Fields
    private static AsgStrategy asgStrategy;
    private static AsgStrategyContext context;
    //endregion
}
