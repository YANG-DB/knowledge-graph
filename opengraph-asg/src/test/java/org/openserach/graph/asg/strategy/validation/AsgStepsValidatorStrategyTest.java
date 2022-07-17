package org.openserach.graph.asg.strategy.validation;

import org.openserach.graph.asg.validation.AsgStepsValidatorStrategy;
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
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.*;
import static org.opensearch.graph.model.query.quant.QuantType.all;

/**
 * Created by lior.perry on 6/6/2017.
 */
public class AsgStepsValidatorStrategyTest {
    Ontology ontology;

    AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
            .next(unTyped(1))
            .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                    RelProp.of(10, START_DATE.type, of(eq, new Date())))))
            .next(unTyped(3))
            .build();

    @Before
    public void setUp() throws Exception {
        ontology = OntologyTestUtils.createDragonsOntologyLong();
    }

    @Test
    public void testValidQuery() {
        AsgStepsValidatorStrategy strategy = new AsgStepsValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }

    @Test
    public void testStepNoRelQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(unTyped(1))
                .next(unTyped(3))
                .build();

        AsgStepsValidatorStrategy strategy = new AsgStepsValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgStepsValidatorStrategy.ERROR_1));
    }

    @Test
    public void testStepWithPropsNoRelQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(unTyped(1))
                .next(ePropGroup(10, EProp.of(11, COLOR.type, of(eq, "Moshe"))))
                .next(unTyped(3))
                .next(ePropGroup(12, EProp.of(13, NAME.type, of(eq, "bubu"))))
                .build();

        AsgStepsValidatorStrategy strategy = new AsgStepsValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgStepsValidatorStrategy.ERROR_1));
    }

    @Test
    public void testStepWithPropsNoEntityQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(rel(1, FREEZE.getrType(), R)
                .below(relProp(2, RelProp.of(3, START_DATE.type, Constraint.of(ge, new Date(System.currentTimeMillis()))))))
                .next(rel(4, FREEZE.getrType(), R)
                .below(relProp(5, RelProp.of(6, START_DATE.type, Constraint.of(ge, new Date(System.currentTimeMillis()))))))
                .next(ePropGroup(7, EProp.of(8, NAME.type, of(eq, "bubu"))))
                .build();

        AsgStepsValidatorStrategy strategy = new AsgStepsValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgStepsValidatorStrategy.ERROR_2));
    }

    @Test
    public void testStepWithPropsNoEntityLongQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(rel(1, FREEZE.getrType(), R))
                .next(quant1(2, QuantType.all))
                .in(ePropGroup(3, EProp.of(3, FIRST_NAME.type, Constraint.of(ConstraintOp.eq, "abc"))),
                        rel(4, OWN.getrType(), Rel.Direction.R).below(relProp(5))
                        .next(rel(6, FREEZE.getrType(), R))
                        .next(typed(10, GUILD.type).next(ePropGroup(11))))
                .build();

        AsgStepsValidatorStrategy strategy = new AsgStepsValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgStepsValidatorStrategy.ERROR_2));
    }

    @Test
    public void testStepWithPropsNoRelLongQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(quant1(2, QuantType.all)).
                in(ePropGroup(3, EProp.of(3, FIRST_NAME.type, Constraint.of(ConstraintOp.eq, "abc"))),
                        rel(4, OWN.getrType(), Rel.Direction.R).below(relProp(5)).
                                next(typed(6, DRAGON.type)
                                        .next(ePropGroup(7)).
                                next(typed(10, GUILD.type).next(ePropGroup(11))))).
                build();

        AsgStepsValidatorStrategy strategy = new AsgStepsValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgStepsValidatorStrategy.ERROR_1));
    }

    @Test
    public void testStepWithQuantAllPropsNoRelLongQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(quant1(2, QuantType.all)).
                in(ePropGroup(3, EProp.of(3, FIRST_NAME.type, Constraint.of(ConstraintOp.eq, "abc"))),
                        rel(4, OWN.getrType(), Rel.Direction.R).below(relProp(5)).
                                next(typed(6, DRAGON.type)
                                        .next(ePropGroup(7))),
                        rel(10, OWN.getrType(), Rel.Direction.R).
                                        next(typed(11, DRAGON.type)
                                                .next(ePropGroup(12)))).
                build();

        AsgStepsValidatorStrategy strategy = new AsgStepsValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }

    @Test
    public void testStepWithQuantSomePropsNoRelLongQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(quant1(2, QuantType.some)).
                in(ePropGroup(3, EProp.of(3, FIRST_NAME.type, Constraint.of(ConstraintOp.eq, "abc"))),
                        rel(4, OWN.getrType(), Rel.Direction.R).below(relProp(5)).
                                next(typed(6, DRAGON.type)
                                        .next(ePropGroup(7))),
                        rel(10, OWN.getrType(), Rel.Direction.R).
                                        next(typed(11, DRAGON.type)
                                                .next(ePropGroup(12)))).
                build();

        AsgStepsValidatorStrategy strategy = new AsgStepsValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }

    @Test
    public void testStepWithRootedQuantSomePropsNoRelLongQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(quant1(1, QuantType.some)).
                in(typed(2, PERSON.type).next(
                        rel(4, OWN.getrType(), Rel.Direction.R).below(relProp(5)).
                                next(typed(6, DRAGON.type)
                                        .next(ePropGroup(7)))),
                        typed(10, PERSON.type).next(
                            rel(111, OWN.getrType(), Rel.Direction.R).
                                            next(typed(12, DRAGON.type)
                                                    .next(ePropGroup(13))))).
                build();

        AsgStepsValidatorStrategy strategy = new AsgStepsValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }
    @Test
    public void testStepWithRootedQuantAllPropsNoRelLongQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(quant1(1, all)).
                in(typed(2, PERSON.type).next(
                        rel(4, OWN.getrType(), Rel.Direction.R).below(relProp(5)).
                                next(typed(6, DRAGON.type)
                                        .next(ePropGroup(7)))),
                        typed(10, PERSON.type).next(
                            rel(111, OWN.getrType(), Rel.Direction.R).
                                            next(typed(12, DRAGON.type)
                                                    .next(ePropGroup(13))))).
                build();

        AsgStepsValidatorStrategy strategy = new AsgStepsValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }

    @Test
    public void testStepWithPropsNoRelLongerQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type)
                        .next(ePropGroup(2, EProp.of(3, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189L)))))
                .next(rel(4, OWN.getrType(), R)
                        .below(relProp(5, RelProp.of(6, START_DATE.type, Constraint.of(ge, new Date(System.currentTimeMillis()))))))
                .next(typed(7, DRAGON.type))
                .next(quant1(8, all))
                .in(ePropGroup(9, EProp.of(10, NAME.type, Constraint.of(ge, "smith")))
                        , rel(12, FREEZE.getrType(), R).below(relProp(122))
                                .next(unTyped(13)
                                        .next(ePropGroup(14, EProp.of(15, NAME.type, Constraint.of(ConstraintOp.notContains, "bob"))))
                                .below(relProp(18, RelProp.of(19, START_DATE.type,
                                        Constraint.of(ge, new Date(System.currentTimeMillis() - 1000 * 60))),
                                        RelProp.of(19, END_DATE.type, Constraint.of(le, new Date(System.currentTimeMillis() + 1000 * 60)))))
                                .next(concrete(20, "smoge", DRAGON.type, "Display:smoge", "D")
                                        .next(ePropGroup(21, EProp.of(22, NAME.type, Constraint.of(ConstraintOp.eq, "smoge")))))
                                )
                ).build();

        AsgStepsValidatorStrategy strategy = new AsgStepsValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgStepsValidatorStrategy.ERROR_1));
    }

}
