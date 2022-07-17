package org.openserach.graph.asg.strategy.validation;

import org.openserach.graph.asg.validation.AsgWhereByConstraintValidatorStrategy;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.RelProp;
import org.opensearch.graph.model.query.properties.constraint.WhereByConstraint;
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
public class AsgWhereByValidationStrategyTest {

    Ontology ontology;

    AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
            .next(typed(1, PERSON.type,"Person"))
            .next(ePropGroup(10, EProp.of(11, FIRST_NAME.type, of(eq, "Moshe"))))
            .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                    RelProp.of(10, START_DATE.type, of(eq, new Date())))))
            .next(concrete(3, "HorseWithNoName", HORSE.type,"display","eTag"))
            .next(ePropGroup(12, EProp.of(13, NAME.type, WhereByConstraint.of(eq, "Person",FIRST_NAME.type))))
            .build();

    @Before
    public void setUp() throws Exception {
        ontology = OntologyTestUtils.createDragonsOntologyLong();
    }

    @Test
    public void testValidQuery() {
        AsgWhereByConstraintValidatorStrategy strategy = new AsgWhereByConstraintValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }

    @Test
    public void testNotValidTag() {
        String PERSON_1 = "Person1";
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type,"Person"))
                .next(ePropGroup(10, EProp.of(11, FIRST_NAME.type, of(eq, "Moshe"))))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(concrete(3, "HorseWithNoName", HORSE.type,"display","eTag"))
                .next(ePropGroup(12, EProp.of(13, NAME.type, WhereByConstraint.of(eq, PERSON_1,FIRST_NAME.type))))
                .build();

        AsgWhereByConstraintValidatorStrategy strategy = new AsgWhereByConstraintValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0]
                .contains(String.format(AsgWhereByConstraintValidatorStrategy.ERROR_2,PERSON_1)));
    }

    @Test
    public void testNotValidField() {
        final String SOME_NAME = "someName";
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type,"Person"))
                .next(ePropGroup(10, EProp.of(11, FIRST_NAME.type, of(eq, "Moshe"))))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(concrete(3, "HorseWithNoName", HORSE.type,"display","eTag"))
                .next(ePropGroup(12, EProp.of(13, NAME.type, WhereByConstraint.of(eq, "Person", SOME_NAME))))
                .build();

        AsgWhereByConstraintValidatorStrategy strategy = new AsgWhereByConstraintValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0]
                .contains(String.format(AsgWhereByConstraintValidatorStrategy.ERROR_3,SOME_NAME)));
    }

    @Test
    public void testNotValidMultiWhereClause() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type,"Person"))
                .next(ePropGroup(10, EProp.of(11, FIRST_NAME.type, WhereByConstraint.of(eq, "Person","someName"))))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(concrete(3, "HorseWithNoName", HORSE.type,"display","eTag"))
                .next(ePropGroup(12, EProp.of(13, NAME.type, WhereByConstraint.of(eq, "Person","someName"))))
                .build();

        AsgWhereByConstraintValidatorStrategy strategy = new AsgWhereByConstraintValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgWhereByConstraintValidatorStrategy.ERROR_1));
    }


}
