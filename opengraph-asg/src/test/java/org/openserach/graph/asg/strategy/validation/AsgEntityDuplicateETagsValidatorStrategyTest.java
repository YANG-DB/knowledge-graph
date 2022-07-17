package org.openserach.graph.asg.strategy.validation;

import org.openserach.graph.asg.validation.AsgEntityDuplicateETagValidatorStrategy;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.model.OntologyTestUtils.HORSE;
import org.opensearch.graph.model.OntologyTestUtils.PERSON;
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
import static org.opensearch.graph.model.OntologyTestUtils.OWN;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.query.Rel.Direction.R;
import static org.opensearch.graph.model.query.properties.constraint.Constraint.of;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.eq;

/**
 * Created by lior.perry on 6/6/2017.
 */
public class AsgEntityDuplicateETagsValidatorStrategyTest {
    Ontology ontology;

    @Before
    public void setUp() throws Exception {
        ontology = OntologyTestUtils.createDragonsOntologyLong();
    }

    @Test
    public void testValidQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type,"P"))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(
                        relProp(10,
                                RelProp.of(10, START_DATE.type, of(eq, new Date())),
                                RelProp.of(11, START_DATE.type, of(eq, new Date())))))
                .next(concrete(3, "HorseWithNoName",HORSE.type,"display","H"))
                .build();
        AsgEntityDuplicateETagValidatorStrategy strategy = new AsgEntityDuplicateETagValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }
    @Test
    public void testValidInOrQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type,"P")).
                next(quant1(2, QuantType.some)).
                in(ePropGroup(3, EProp.of(3, FIRST_NAME.type, Constraint.of(ConstraintOp.eq, "abc"))),
                        rel(4, OWN.getrType(), Rel.Direction.R).below(relProp(5)).
                                next(typed(6, DRAGON.type,"D")
                                        .next(ePropGroup(7))),
                        rel(10, OWN.getrType(), Rel.Direction.R).
                                next(typed(11, DRAGON.type,"D")
                                        .next(ePropGroup(12)))).
                build();

        AsgEntityDuplicateETagValidatorStrategy strategy = new AsgEntityDuplicateETagValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertTrue(validationResult.valid());
    }
    @Test
    public void testNonValidInOrQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type,"P")).
                next(quant1(2, QuantType.some)).
                in(ePropGroup(3, EProp.of(3, FIRST_NAME.type, Constraint.of(ConstraintOp.eq, "abc"))),
                        rel(4, OWN.getrType(), Rel.Direction.R).below(relProp(5)).
                                next(typed(6, DRAGON.type,"D")
                                        .next(ePropGroup(7))),
                        rel(10, KNOW.getrType(), Rel.Direction.R).
                                next(typed(11, PERSON.type,"D")
                                        .next(ePropGroup(12)))).
                build();

        AsgEntityDuplicateETagValidatorStrategy strategy = new AsgEntityDuplicateETagValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(String.format(AsgEntityDuplicateETagValidatorStrategy.ERROR_1,"D")));
    }
    @Test
    public void testNonValidInOrRelQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type,"P")).
                next(quant1(2, QuantType.some)).
                in(ePropGroup(3, EProp.of(3, FIRST_NAME.type, Constraint.of(ConstraintOp.eq, "abc"))),
                        rel(4, OWN.getrType(), Rel.Direction.R,"Own").below(relProp(5)).
                                next(typed(6, DRAGON.type,"D")
                                        .next(ePropGroup(7))),
                        rel(10, KNOW.getrType(), Rel.Direction.R,"Own").
                                next(typed(11, PERSON.type,"P")
                                        .next(ePropGroup(12)))).
                build();

        AsgEntityDuplicateETagValidatorStrategy strategy = new AsgEntityDuplicateETagValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(String.format(AsgEntityDuplicateETagValidatorStrategy.ERROR_1,"Own")));
    }

    @Test
    public void testNotValidConcreteEntityTypeQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(concrete(1, "no", "100", "eName", "eTag"))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(typed(3,"eTag","eTag"))
                .build();

        AsgEntityDuplicateETagValidatorStrategy strategy = new AsgEntityDuplicateETagValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(String.format(AsgEntityDuplicateETagValidatorStrategy.ERROR_1,"eTag")));
    }

    @Test
    public void testNotValidTypedEntityTypeQuery() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, PERSON.type,"P"))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(typed(3, "100","P"))
                .next(rel(4, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .build();

        AsgEntityDuplicateETagValidatorStrategy strategy = new AsgEntityDuplicateETagValidatorStrategy();
        ValidationResult validationResult = strategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Assert.assertFalse(validationResult.valid());
        Assert.assertTrue(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)[0].contains(String.format(AsgEntityDuplicateETagValidatorStrategy.ERROR_1,"P")));
    }
}
