package org.opensearch.graph.asg.strategy.selection;

import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.RelProp;
import org.opensearch.graph.model.query.properties.projection.IdentityProjection;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.opensearch.graph.model.OntologyTestUtils.*;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.query.Rel.Direction.R;
import static org.opensearch.graph.model.query.properties.constraint.Constraint.of;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.eq;

public class DefaultSelectionAsgStrategyTest {
    private Ontology ontology;

    @Before
    public void setUp() throws Exception {
        ontology = OntologyTestUtils.createDragonsOntologyLong();
    }

    @Test
    public void testSelectionForTypedEntity() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, OntologyTestUtils.PERSON.type))
                .next(ePropGroup(10, EProp.of(11, FIRST_NAME.type, of(eq, "Moshe"))))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(20,RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(concrete(3, "HorseWithNoName", OntologyTestUtils.HORSE.type,"display","eTag"))
                .next(ePropGroup(12, EProp.of(13, NAME.type, of(eq, "bubu"))))
                .build();

        DefaultSelectionAsgStrategy selectionAsgStrategy = new DefaultSelectionAsgStrategy(new OntologyProvider() {
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
        });

        selectionAsgStrategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Optional<AsgEBase<EPropGroup>> ePropGroupAsgEBase = AsgQueryUtil.element(query, 10);
        Assert.assertTrue(ePropGroupAsgEBase.isPresent());
        for (Property prop : PERSON.propertyList) {

            Assert.assertFalse(Stream.ofAll(ePropGroupAsgEBase.get().geteBase().getProps()).find(rp -> rp.getpType().equals(prop.type) && rp.getProj() instanceof IdentityProjection).isEmpty());

        }
    }

    @Test
    public void testNoSelectionForTypedEntityWithProj() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, OntologyTestUtils.PERSON.type))
                .next(ePropGroup(10, EProp.of(11, FIRST_NAME.type, of(eq, "Moshe")), EProp.of(100, "firstName", new IdentityProjection())))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(20,RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(concrete(3, "HorseWithNoName", OntologyTestUtils.HORSE.type,"display","eTag"))
                .next(ePropGroup(12, EProp.of(13, NAME.type, of(eq, "bubu"))))
                .build();

        DefaultSelectionAsgStrategy selectionAsgStrategy = new DefaultSelectionAsgStrategy(new OntologyProvider() {
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
        });

        selectionAsgStrategy.apply(query, new AsgStrategyContext(new Ontology.Accessor(ontology)));
        Optional<AsgEBase<EPropGroup>> ePropGroupAsgEBase = AsgQueryUtil.element(query, 10);
        Assert.assertTrue(ePropGroupAsgEBase.isPresent());
        Assert.assertEquals(2, ePropGroupAsgEBase.get().geteBase().getProps().size());
    }
}
