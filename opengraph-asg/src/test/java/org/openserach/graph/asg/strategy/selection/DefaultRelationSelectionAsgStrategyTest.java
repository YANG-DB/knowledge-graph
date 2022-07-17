package org.openserach.graph.asg.strategy.selection;

import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.RelProp;
import org.opensearch.graph.model.query.properties.RelPropGroup;
import org.opensearch.graph.model.query.properties.projection.IdentityProjection;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.opensearch.graph.model.OntologyTestUtils.FIRST_NAME;
import static org.opensearch.graph.model.OntologyTestUtils.NAME;
import static org.opensearch.graph.model.OntologyTestUtils.START_DATE;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.concrete;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.ePropGroup;
import static org.opensearch.graph.model.query.Rel.Direction.R;
import static org.opensearch.graph.model.query.properties.constraint.Constraint.of;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.eq;

public class DefaultRelationSelectionAsgStrategyTest {
    private Ontology ontology;

    @Before
    public void setUp() throws Exception {
        ontology = OntologyTestUtils.createDragonsOntologyLong();
    }

//    @Test //todo FIXME
    @Ignore("FIXME: 22/06/2021")
   public void FIXME_testSelectionForRel() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, OntologyTestUtils.PERSON.type))
                .next(AsgQuery.Builder.ePropGroup(10, EProp.of(11, FIRST_NAME.type, of(eq, "Moshe"))))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(20,RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(concrete(3, "HorseWithNoName", OntologyTestUtils.HORSE.type,"display","eTag"))
                .next(AsgQuery.Builder.ePropGroup(12, EProp.of(13, NAME.type, of(eq, "bubu"))))
                .build();

        DefaultRelationSelectionAsgStrategy selectionAsgStrategy = new DefaultRelationSelectionAsgStrategy(new OntologyProvider() {
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
        Optional<AsgEBase<RelPropGroup>> relPropGroup = AsgQueryUtil.element(query, 20);
        Assert.assertTrue(relPropGroup.isPresent());
        for (String prop : OntologyTestUtils.OWN.getProperties()) {

            Assert.assertFalse(Stream.ofAll(relPropGroup.get().geteBase().getProps()).find(rp -> rp.getpType().equals(prop) && rp.getProj() instanceof IdentityProjection).isEmpty());

        }
    }

    @Test
    public void testNoSelectionForRelationWithProj() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons")
                .next(typed(1, OntologyTestUtils.PERSON.type))
                .next(AsgQuery.Builder.ePropGroup(10, EProp.of(11, FIRST_NAME.type, of(eq, "Moshe"))))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(20,RelProp.of(10, START_DATE.type, of(eq, new Date())),
                        RelProp.of(100, "startDate", new IdentityProjection()))))
                .next(concrete(3, "HorseWithNoName", OntologyTestUtils.HORSE.type,"display","eTag"))
                .next(AsgQuery.Builder.ePropGroup(12, EProp.of(13, NAME.type, of(eq, "bubu"))))
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
        Optional<AsgEBase<RelPropGroup>> relPropAsgEBase = AsgQueryUtil.element(query, 20);
        Assert.assertTrue(relPropAsgEBase.isPresent());
        Assert.assertEquals(2, relPropAsgEBase.get().geteBase().getProps().size());
    }
}
