package org.opensearch.graph.asg.strategy.validation;

import org.opensearch.graph.asg.validation.AsgOntologyRelValidatorStrategy;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.RelProp;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.opensearch.graph.model.query.quant.QuantType;
import org.opensearch.graph.model.validation.ValidationResult;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.opensearch.graph.model.OntologyTestUtils.*;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.query.Rel.Direction.R;
import static org.opensearch.graph.model.query.properties.constraint.Constraint.of;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.eq;

/**
 * Created by lior.perry on 6/6/2017.
 */
public class AsgOntologyRelValidatorStrategyTest {
    Ontology ontology;

    AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
            .next(typed(1, PERSON.type))
            .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                    RelProp.of(10, START_DATE.type, of(eq, new Date())))))
            .next(concrete(3, "HorseWithNoName", HORSE.type, "display", "eTag"))
            .build();

    @Before
    public void setUp() throws Exception {
        ontology = OntologyTestUtils.createDragonsOntologyLong();
    }

    @Test
    public void testValidParentEntityWithQuantUntypedQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(unTyped(1, DRAGON.type, HORSE.type)).
                next(quant1(2, QuantType.all)).
                in(ePropGroup(3, EProp.of(3, GENDER.type, Constraint.of(ConstraintOp.eq, Gender.MALE))),
                        rel(8, REGISTERED.getrType(), Rel.Direction.R).below(relProp(9)).
                                next(typed(10, GUILD.type)
                                        .next(ePropGroup(11,
                                                EProp.of(11, NAME.type, Constraint.of(ConstraintOp.eq, "abc")))))).
                build();

        AsgOntologyRelValidatorStrategy strategy = new AsgOntologyRelValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }

    @Test
    public void testNotValidDirectionParentEntityWithQuantUntypedQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(unTyped(1, DRAGON.type, HORSE.type)).
                next(quant1(2, QuantType.all)).
                in(ePropGroup(3, EProp.of(3, GENDER.type, Constraint.of(ConstraintOp.eq, Gender.MALE))),
                        rel(8, REGISTERED.getrType(), Rel.Direction.L).below(relProp(9)).
                                next(typed(10, GUILD.type)
                                        .next(ePropGroup(11,
                                                EProp.of(11, NAME.type, Constraint.of(ConstraintOp.eq, "abc")))))).
                build();

        AsgOntologyRelValidatorStrategy strategy = new AsgOntologyRelValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgOntologyRelValidatorStrategy.ERROR_2));
    }

    @Test
    public void testValidParentEntityWithQuantNonValidUntypedQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(unTyped(1, DRAGON.type, HORSE.type)).
                next(quant1(2, QuantType.all)).
                in(ePropGroup(3, EProp.of(3, GENDER.type, Constraint.of(ConstraintOp.eq, Gender.MALE))),
                        rel(8, REGISTERED.getrType(), Rel.Direction.R).below(relProp(9)).
                                next(typed(10, GUILD.type)
                                        .next(ePropGroup(11,
                                                EProp.of(11, NAME.type, Constraint.of(ConstraintOp.eq, "abc")))))).
                build();

        AsgOntologyRelValidatorStrategy strategy = new AsgOntologyRelValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }

    @Test
    public void testValidParentEntityWithQuantReverseQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, DRAGON.type)).
                next(quant1(2, QuantType.all)).
                in(ePropGroup(3, EProp.of(3, NAME.type, Constraint.of(ConstraintOp.le, "abc"))),
                        rel(8, FIRE.getrType(), Rel.Direction.L).below(relProp(9)).
                                next(typed(10, DRAGON.type)
                                        .next(ePropGroup(11, EProp.of(11, NAME.type, Constraint.of(ConstraintOp.eq, "abc"))))),
                        rel(4, FREEZE.getrType(), Rel.Direction.R).below(relProp(5)).
                                next(typed(6, DRAGON.type)
                                        .next(ePropGroup(7, EProp.of(7, NAME.type, Constraint.of(ConstraintOp.eq, "abc")))))).
                build();

        AsgOntologyRelValidatorStrategy strategy = new AsgOntologyRelValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }

    @Test
    public void testValidParentEntityWithQuantQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, DRAGON.type)).
                next(quant1(2, QuantType.all)).
                in(ePropGroup(3, EProp.of(3, NAME.type, Constraint.of(ConstraintOp.le, "abc"))),
                        rel(8, FIRE.getrType(), Rel.Direction.R).below(relProp(9)).
                                next(typed(10, DRAGON.type)
                                        .next(ePropGroup(11, EProp.of(11, NAME.type, Constraint.of(ConstraintOp.eq, "abc"))))),
                        rel(4, FREEZE.getrType(), Rel.Direction.R).below(relProp(5)).
                                next(typed(6, DRAGON.type)
                                        .next(ePropGroup(7, EProp.of(7, NAME.type, Constraint.of(ConstraintOp.eq, "abc")))))).
                build();

        AsgOntologyRelValidatorStrategy strategy = new AsgOntologyRelValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }

    @Test
    public void testNotValidNoParentEntityWithQuantQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(quant1(2, QuantType.all)).
                in(ePropGroup(3, EProp.of(3, NAME.type, Constraint.of(ConstraintOp.le, "abc"))),
                        rel(8, FIRE.getrType(), Rel.Direction.R).below(relProp(9)).
                                next(typed(10, DRAGON.type).next(ePropGroup(11, EProp.of(11, NAME.type, Constraint.of(ConstraintOp.eq, "abc"))))),
                        rel(4, FREEZE.getrType(), Rel.Direction.R).below(relProp(5)).
                                next(typed(6, DRAGON.type)
                                        .next(ePropGroup(7, EProp.of(7, NAME.type, Constraint.of(ConstraintOp.eq, "abc")))))).
                build();

        AsgOntologyRelValidatorStrategy strategy = new AsgOntologyRelValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgOntologyRelValidatorStrategy.ERROR_1));
    }

    @Test
    public void testValidTypeQuery() {
        AsgOntologyRelValidatorStrategy strategy = new AsgOntologyRelValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }

    @Test
    public void testValidUntypedOneSideQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(unTyped(1))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(concrete(3, "HorseWithNoName", HORSE.type, "display", "eTag"))
                .build();
        AsgOntologyRelValidatorStrategy strategy = new AsgOntologyRelValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }

    @Test
    public void testValidUntypedTowSidesQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(unTyped(1))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(unTyped(3))
                .build();
        AsgOntologyRelValidatorStrategy strategy = new AsgOntologyRelValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }

    @Test
    public void testNotValidRelWithOneSideMissingQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(concrete(3, "HorseWithNoName", HORSE.type, "display", "eTag"))
                .build();
        AsgOntologyRelValidatorStrategy strategy = new AsgOntologyRelValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgOntologyRelValidatorStrategy.ERROR_1));

    }

    @Test
    public void testValidRelWithQuantBeforeRelQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(unTyped(1)).
                next(quant1(2, QuantType.all))
                        .next(rel(3, FREEZE.getrType(), Rel.Direction.R).below(relProp(5)))
                                .next(quant1(4, QuantType.all))
                                .next(typed(5, DRAGON.type))
                                    .next(quant1(6, QuantType.all))
                                        .in(ePropGroup(7, EProp.of(7, NAME.type, Constraint.of(ConstraintOp.eq, "abc"))))
                .build();
        AsgOntologyRelValidatorStrategy strategy = new AsgOntologyRelValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.toString(),validationResult.valid());

    }

    @Test
    public void testNotValidRelWithOneSideNotAllowedByRelTypesQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(unTyped(1, PERSON.type))
                .next(rel(2, OntologyTestUtils.FIRE.getrType(), R).below(relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(concrete(3, "HorseWithNoName", HORSE.type, "display", "eTag"))
                .build();

        AsgOntologyRelValidatorStrategy strategy = new AsgOntologyRelValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgOntologyRelValidatorStrategy.ERROR_2));

    }

    @Test
    public void testNotValidRelWithTwoSidesNotAllowedByRelTypesQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(rel(2, OntologyTestUtils.FIRE.getrType(), R).below(relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(concrete(3, "HorseWithNoName", HORSE.type, "display", "eTag"))
                .build();

        AsgOntologyRelValidatorStrategy strategy = new AsgOntologyRelValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgOntologyRelValidatorStrategy.ERROR_2));

    }

}
