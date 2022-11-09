package org.opensearch.graph.asg.strategy.schema;

import junit.framework.TestCase;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opensearch.graph.asg.strategy.AsgStrategy;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.*;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.NestedEProp;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.opensearch.graph.model.schema.BaseTypeElement;
import org.opensearch.graph.unipop.schema.providers.GraphElementConstraint;
import org.opensearch.graph.unipop.schema.providers.GraphElementPropertySchema;
import org.opensearch.graph.unipop.schema.providers.GraphElementSchemaProvider;
import org.opensearch.graph.unipop.schema.providers.GraphVertexSchema;

import java.util.*;

import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.query.quant.QuantType.all;

/**
 * test the step reducer strategy
 */
public class AsgStepReductionStrategyTest  {

    //region Setup
    @BeforeClass
    public static void setup() {
        Ontology ontology = Ontology.OntologyBuilder.anOntology("ont")
                .withEntityTypes(Arrays.asList(
                        EntityType.Builder.get().withEType("Person").withName("Person")
                                .withProperties(Arrays.asList("name", "title", "dragons"))
                                .build(),
                        EntityType.Builder.get().withEType("Dragon").withName("Dragon")
                                .withProperties(Arrays.asList("name", "age")).build())
                ).withRelationshipTypes(Collections.singletonList(
                        RelationshipType.Builder.get().withName("hasDragon")
                                .withEPairs(Collections.singletonList(new EPair("Person", "Dragon")))
                                .build()))
                .withProperties(new HashSet<>(Arrays.asList(
                        Property.Builder.get().withPType("age").withName("age").withType("int").build(),
                        Property.Builder.get().withPType("name").withName("name").withType("text").build(),
                        Property.Builder.get().withPType("dragons").withName("dragons").withType("Dragon").build())))
                .build();

        GraphElementSchemaProvider schemaProvider = new GraphElementSchemaProvider.Impl(
                Arrays.asList(
                        new GraphVertexSchema.Impl(
                                BaseTypeElement.Type.of("Person"),
                                new GraphElementConstraint.Impl(__.start()),
                                Optional.empty(),
                                Optional.empty(),
                                Arrays.asList(
                                        new GraphElementPropertySchema.Impl("name", "name", "text", Collections.singletonList(
                                                new GraphElementPropertySchema.ExactIndexingSchema.Impl("name.keyword")
                                        )),
                                        new GraphElementPropertySchema.Impl("dragons", "dragons", "Dragon", Collections.singletonList(
                                                //this is the nested "path" section
                                                new GraphElementPropertySchema.NestedIndexingSchema.Impl("dragons")
                                        )),
                                        new GraphElementPropertySchema.Impl("dragons.name", "dragons.name", "text", Arrays.asList(
                                                new GraphElementPropertySchema.ExactIndexingSchema.Impl("dragons.name.keyword"),
                                                new GraphElementPropertySchema.NestedIndexingSchema.Impl("dragons")
                                        )),
                                        new GraphElementPropertySchema.Impl("dragons.age", "dragons.age", "int", Collections.singletonList(
                                                //this is the nested "path" section
                                                new GraphElementPropertySchema.NestedIndexingSchema.Impl("dragons")
                                        )))
                        ),
                        new GraphVertexSchema.Impl(
                                BaseTypeElement.Type.of("Dragon"),
                                new GraphElementConstraint.Impl(__.start()),
                                Optional.empty(),
                                Optional.empty(),
                                Collections.singletonList(
                                        new GraphElementPropertySchema.Impl("name", "name", "text", Collections.singletonList(
                                                new GraphElementPropertySchema.ExactIndexingSchema.Impl("name.keyword")
                                        ))
                                )
                        )
                ),
                Collections.emptyList(),
                Arrays.asList(
                        new GraphElementPropertySchema.Impl("name", "name", "text", Collections.singletonList(
                                new GraphElementPropertySchema.ExactIndexingSchema.Impl("name.keyword")
                        )),
                        new GraphElementPropertySchema.Impl("name", "dragons.name", "text", Arrays.asList(
                                new GraphElementPropertySchema.ExactIndexingSchema.Impl("name.keyword"),
                                new GraphElementPropertySchema.NestedIndexingSchema.Impl("dragons")
                        )),
                        new GraphElementPropertySchema.Impl("age", "dragons.age", "int", Arrays.asList(
                                new GraphElementPropertySchema.NestedIndexingSchema.Impl("dragons")
                        )),
                        new GraphElementPropertySchema.Impl("dragons", "dragons", "Dragon", Collections.singletonList(
                                //this is the nested "path" section
                                new GraphElementPropertySchema.NestedIndexingSchema.Impl("dragons")
                        ))
                )
        );

        GraphElementSchemaProviderFactory schemaProviderFactory = ontology1 -> schemaProvider;

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

        stepReduceStrategy = new AsgStepReductionStrategy(ontologyProvider,schemaProviderFactory);
        context = new AsgStrategyContext(new Ontology.Accessor(ontology));
    }
    //endregion


    @Test
//    @Ignore("TODO - Complete the strategy implementation before returning this test to operative status")
    public void testSimpleOneStepNoConstraintsReducer() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "dragons.name", Constraint.of(ConstraintOp.eq, "Sherley"))))
                .build();

        stepReduceStrategy.apply(asgQuery, context);

        EPropGroup actual = AsgQueryUtil.<EPropGroup>element(asgQuery, 3).get().geteBase();
        EPropGroup expected = new EPropGroup(3, Arrays.asList(
                new NestedEProp(3, "dragons.name", Constraint.of(ConstraintOp.eq, "Sherley"), "dragons")));

//        Assert.assertEquals(expected, actual);

    }

    private static AsgStrategy stepReduceStrategy;
    private static AsgStrategyContext context;
}