package org.openserach.graph.asg.strategy.type;

import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.entity.EUntyped;
import org.opensearch.graph.model.query.properties.RelProp;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import static org.opensearch.graph.model.OntologyTestUtils.START_DATE;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.query.properties.constraint.Constraint.of;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.eq;
import static org.opensearch.graph.model.query.Rel.Direction.R;

/**
 * Created by benishue on 24-Apr-17.
 */
public class UntypedInferTypeLeftSideRelationAsgStrategyTest {
    //This Eprop is not under an AND quantifier and should be replaced by the EPropGroup Element -  e.g. Q3 on V1


    @Test
    public void testUntypedToTypedStrategy() throws Exception {
        Ontology.Accessor ont = new Ontology.Accessor(OntologyTestUtils.createDragonsOntologyShort());
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragon")
                .next(unTyped(1))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(typed(3, OntologyTestUtils.DRAGON.type))
                .build();

        AsgEBase<EUntyped> next = AsgQueryUtil.element$(query, EUntyped.class);
        Assert.assertTrue(next.geteBase().getvTypes().isEmpty());

        UntypedInferTypeLeftSideRelationAsgStrategy strategy = new UntypedInferTypeLeftSideRelationAsgStrategy();
        strategy.apply(query,new AsgStrategyContext(ont));

        AsgEBase<EUntyped> after = AsgQueryUtil.<EUntyped>elements(query, EUntyped.class).iterator().next();
        Assert.assertEquals(after.geteBase().getvTypes(), Collections.singleton(OntologyTestUtils.PERSON.type));



    }

    @Test
    public void testUntypedToConcreteStrategy() throws Exception {
        Ontology.Accessor ont = new Ontology.Accessor(OntologyTestUtils.createDragonsOntologyLong());
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragon")
                .next(unTyped(1))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(concrete(3, "123",OntologyTestUtils.DRAGON.type,"",""))
                .build();

        AsgEBase<EUntyped> next = AsgQueryUtil.element$(query, EUntyped.class);
        Assert.assertTrue(next.geteBase().getvTypes().isEmpty());

        UntypedInferTypeLeftSideRelationAsgStrategy strategy = new UntypedInferTypeLeftSideRelationAsgStrategy();
        strategy.apply(query,new AsgStrategyContext(ont));

        AsgEBase<EUntyped> after = AsgQueryUtil.<EUntyped>elements(query, EUntyped.class).iterator().next();
        Assert.assertEquals(after.geteBase().getvTypes(), Collections.singleton(OntologyTestUtils.PERSON.type));



    }

    @Test
    public void testUntypedToUntypedStrategy() throws Exception {
        Ontology.Accessor ont = new Ontology.Accessor(OntologyTestUtils.createDragonsOntologyLong());
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragon")
                .next(unTyped(1))
                .next(rel(2, OntologyTestUtils.OWN.getrType(), R).below(relProp(10,
                        RelProp.of(10, START_DATE.type, of(eq, new Date())))))
                .next(unTyped(3))
                .build();

        AsgEBase<EUntyped> next = AsgQueryUtil.element$(query, EUntyped.class);
        Assert.assertTrue(next.geteBase().getvTypes().isEmpty());

        UntypedInferTypeLeftSideRelationAsgStrategy strategy = new UntypedInferTypeLeftSideRelationAsgStrategy();
        strategy.apply(query,new AsgStrategyContext(ont));

        Iterator<AsgEBase<EUntyped>> iterator = AsgQueryUtil.<EUntyped>elements(query, EUntyped.class).iterator();
        AsgEBase<EUntyped> afterSideA = iterator.next();
        AsgEBase<EUntyped> afterSideB = iterator.next();

        Assert.assertEquals(Collections.singleton(OntologyTestUtils.PERSON.type), afterSideA.geteBase().getvTypes());
        Assert.assertTrue(afterSideB.geteBase().getvTypes().contains(OntologyTestUtils.DRAGON.type) &&
                afterSideB.geteBase().getvTypes().contains(OntologyTestUtils.HORSE.type));

    }
}