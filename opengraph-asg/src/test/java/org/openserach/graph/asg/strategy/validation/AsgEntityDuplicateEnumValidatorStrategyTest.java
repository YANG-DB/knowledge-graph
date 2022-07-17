package org.openserach.graph.asg.strategy.validation;

import org.openserach.graph.asg.validation.AsgEntityDuplicateEnumValidatorStrategy;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.model.OntologyTestUtils.HORSE;
import org.opensearch.graph.model.OntologyTestUtils.PERSON;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.properties.RelProp;
import org.opensearch.graph.model.validation.ValidationResult;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.opensearch.graph.model.OntologyTestUtils.START_DATE;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.query.Rel.Direction.R;
import static org.opensearch.graph.model.query.properties.constraint.Constraint.of;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.eq;

/**
 * Created by lior.perry on 6/6/2017.
 */
public class AsgEntityDuplicateEnumValidatorStrategyTest {
    Ontology ontology;

    AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
            .next(typed(1, PERSON.type))
            .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(
                    relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date())),
                        RelProp.of(11, START_DATE.type, of(eq, new Date())))))
            .next(concrete(3, "HorseWithNoName",HORSE.type,"display","eTag"))
            .build();

    @Before
    public void setUp() throws Exception {
        ontology = OntologyTestUtils.createDragonsOntologyLong();
    }

    @Test
    public void testValidQuery() {
        AsgEntityDuplicateEnumValidatorStrategy strategy = new AsgEntityDuplicateEnumValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }

    @Test
    public void testNotValidConcreteEntityTypeQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(concrete(1, "no", "100", "eName", "eTag"))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(unTyped(2))
                .build();

        AsgEntityDuplicateEnumValidatorStrategy strategy = new AsgEntityDuplicateEnumValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgEntityDuplicateEnumValidatorStrategy.ERROR_1));
    }

    @Test
    public void testNotValidTypedEntityTypeQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(typed(3, "100"))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .build();

        AsgEntityDuplicateEnumValidatorStrategy strategy = new AsgEntityDuplicateEnumValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(AsgEntityDuplicateEnumValidatorStrategy.ERROR_1));
    }
}
