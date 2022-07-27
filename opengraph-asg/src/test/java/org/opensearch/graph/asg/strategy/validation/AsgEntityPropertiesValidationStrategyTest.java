package org.opensearch.graph.asg.strategy.validation;

import org.opensearch.graph.asg.validation.AsgEntityPropertiesValidatorStrategy;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.aggregation.AggLOp;
import org.opensearch.graph.model.query.properties.CalculatedEProp;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.RelProp;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.opensearch.graph.model.query.properties.projection.CalculatedFieldProjection;
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
public class AsgEntityPropertiesValidationStrategyTest {
    Ontology ontology;

    AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
            .next(typed(1, PERSON.type,"p"))
            .next(ePropGroup(10,
                    EProp.of(11, FIRST_NAME.type, of(eq, "Moshe")),
                    CalculatedEProp.of(101, "p->eTag", new CalculatedFieldProjection(AggLOp.count))))
            .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                    RelProp.of(10, START_DATE.type, of(eq, new Date())))))
            .next(concrete(3, "HorseWithNoName", HORSE.type,"display","eTag"))
            .next(ePropGroup(12, EProp.of(13, NAME.type, of(eq, "bubu"))))
            .build();

    @Before
    public void setUp() throws Exception {
        ontology = OntologyTestUtils.createDragonsOntologyLong();
    }

    @Test
    public void testValidQuery() {
        AsgEntityPropertiesValidatorStrategy strategy = new AsgEntityPropertiesValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }

    @Test
    public void testNotValidNoParentEntityQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(ePropGroup(10, EProp.of(11, FIRST_NAME.type, of(eq, "Moshe"))))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(concrete(3, "HorseWithNoName", HORSE.type,"display","eTag"))
                .next(ePropGroup(12, EProp.of(13, NAME.type, of(eq, "bubu"))))
                .build();

        AsgEntityPropertiesValidatorStrategy strategy = new AsgEntityPropertiesValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgEntityPropertiesValidatorStrategy.ERROR_1));
    }

    @Test
    public void testNotValidNoParentEntityWithQuantQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(quant1(2, QuantType.all)).
                in(ePropGroup(3, EProp.of(3, NAME.type, Constraint.of(ConstraintOp.le,"abc"))),
                        rel(8, FIRE.getrType(), Rel.Direction.R).below(relProp(9)).
                                next(typed(10, DRAGON.type).next(ePropGroup(11, EProp.of(11, NAME.type, Constraint.of(ConstraintOp.eq,"abc"))))),
                        rel(4, FREEZE.getrType(), Rel.Direction.R).below(relProp(5)).
                                next(typed(6, DRAGON.type)
                                        .next(ePropGroup(7, EProp.of(7, NAME.type, Constraint.of(ConstraintOp.eq,"abc")))))).
                build();

        AsgEntityPropertiesValidatorStrategy strategy = new AsgEntityPropertiesValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgEntityPropertiesValidatorStrategy.ERROR_1));
    }

    @Test
    public void testNotValidPropEntityMismatchQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(ePropGroup(10, EProp.of(11, COLOR.type, of(eq, "Moshe"))))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(concrete(3, "HorseWithNoName", HORSE.type,"display","eTag"))
                .next(ePropGroup(12, EProp.of(13, NAME.type, of(eq, "bubu"))))
                .build();

        AsgEntityPropertiesValidatorStrategy strategy = new AsgEntityPropertiesValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgEntityPropertiesValidatorStrategy.ERROR_2));
    }
    @Test
    public void testNotValidQuantPropGroupEntityMismatchQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(quant1(2, QuantType.all))
                .in(relPropGroup(3, RelProp.of(3, NAME.type, Constraint.of(ConstraintOp.le,"abc"))),
                    rel(4, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                            RelProp.of(10, START_DATE.type, of(eq, new Date()))))
                    .next(concrete(5, "HorseWithNoName", HORSE.type,"display","eTag"))
                    .next(ePropGroup(12, EProp.of(13, NAME.type, of(eq, "bubu")))))
                .build();

        AsgEntityPropertiesValidatorStrategy strategy = new AsgEntityPropertiesValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgEntityPropertiesValidatorStrategy.ERROR_1));
    }

    @Test
    public void testNotValidQuantPropEntityMismatchQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(quant1(2, QuantType.all))
                .in(relProp(3, RelProp.of(3, NAME.type, Constraint.of(ConstraintOp.le,"abc"))),
                    rel(4, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                            RelProp.of(10, START_DATE.type, of(eq, new Date()))))
                    .next(concrete(5, "HorseWithNoName", HORSE.type,"display","eTag"))
                    .next(ePropGroup(12, EProp.of(13, NAME.type, of(eq, "bubu")))))
                .build();

        AsgEntityPropertiesValidatorStrategy strategy = new AsgEntityPropertiesValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgEntityPropertiesValidatorStrategy.ERROR_1));
    }

    @Test
    public void testNotValidPropRelMismatchQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(ePropGroup(10, EProp.of(11, FIRST_NAME.type, of(eq, "Moshe"))))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(unTyped(3, HORSE.type, DRAGON.type))
                .next(ePropGroup(12, EProp.of(13, FIRST_NAME.type, of(eq, "bubu"))))
                .build();

        AsgEntityPropertiesValidatorStrategy strategy = new AsgEntityPropertiesValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgEntityPropertiesValidatorStrategy.ERROR_2));
    }
    @Test
    public void testNotValidPropConstraintIntervalTypeQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(ePropGroup(10, EProp.of(11, FIRST_NAME.type, of(eq, "Moshe",null))))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(unTyped(3, HORSE.type, DRAGON.type))
                .next(ePropGroup(12, EProp.of(13, FIRST_NAME.type, of(eq, "bubu"))))
                .build();

        AsgEntityPropertiesValidatorStrategy strategy = new AsgEntityPropertiesValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertEquals(Stream.ofAll(validationResult.errors()).toJavaArray(String.class).length, 2);
    }


}
