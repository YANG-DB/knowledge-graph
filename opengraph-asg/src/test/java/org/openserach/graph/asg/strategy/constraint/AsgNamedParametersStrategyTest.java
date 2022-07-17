package org.openserach.graph.asg.strategy.constraint;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openserach.graph.asg.strategy.AsgNamedParametersStrategy;
import org.opensearch.graph.dispatcher.asg.QueryToAsgTransformer;
import org.opensearch.graph.dispatcher.asg.QueryToCompositeAsgTransformer;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.model.asgQuery.*;
import org.opensearch.graph.model.execution.plan.descriptors.AsgQueryDescriptor;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.Start;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.constraint.*;
import org.opensearch.graph.model.query.quant.Quant1;
import org.opensearch.graph.model.query.quant.QuantBase;
import org.opensearch.graph.model.query.quant.QuantType;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

import static org.opensearch.graph.model.OntologyTestUtils.*;
import static org.opensearch.graph.model.query.Rel.Direction.R;
import static org.opensearch.graph.model.query.quant.QuantType.some;

public class AsgNamedParametersStrategyTest {
    //region Setup
    @Before
    public void setUp() throws Exception {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("Dragons_Ontology.json");
        StringWriter writer = new StringWriter();
        IOUtils.copy(stream, writer);
        Ontology ontology = new ObjectMapper().readValue(writer.toString(), Ontology.class);
        ont = new Ontology.Accessor(ontology);
        asgSupplier = new QueryToCompositeAsgTransformer(new OntologyProvider() {
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

    }
    //endregion

    private Query Q1() {
        return Query.Builder.instance().withName("q1").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "P1", OntologyTestUtils.PERSON.type, 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 6), 0),
                        new Rel(3, OWN.getrType(), R, null, 4, 0),
                        new ETyped(4, "V1", OntologyTestUtils.DRAGON.name, 0, 0),
                        new Rel(6, MEMBER_OF.getrType(), R, null, 7, 0),
                        new ETyped(7, "E2", OntologyTestUtils.DRAGON.name, 9, 0),
                        new Rel(9, FIRE.getrType() , R, null, 10, 0),
                        new ETyped(10, "V2", HORSE.type, 11, 0),
                        new EProp(11,BIRTH_DATE.type, WhereByConstraint.of(ConstraintOp.gt, "P1","creationTime"))
                )).build();
    }


    private Query Q2() {
        return Query.Builder.instance().withName("q2").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "P1", OntologyTestUtils.PERSON.type, 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(20, 3, 6), 0),
                        new EProp(20, PERSON.name, WhereByConstraint.of(ConstraintOp.contains, "Jimmy")),
                        new Rel(3, OWN.getrType(), R, null, 4, 0),
                        new ETyped(4, "V1", OntologyTestUtils.DRAGON.name, 0, 0),
                        new Rel(6, MEMBER_OF.getrType(), R, null, 7, 0),
                        new ETyped(7, "E2", OntologyTestUtils.DRAGON.name, 9, 0),
                        new Rel(9, FIRE.getrType() , R, null, 10, 0),
                        new ETyped(10, "V2", HORSE.type, 11, 0),
                        new EProp(11,BIRTH_DATE.type, WhereByConstraint.of(ConstraintOp.gt, "P1","creationTime"))
                )).build();
    }


    private Query Q3() {
        Query query = Query.Builder.instance().withName("q1").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "People", "Person", 2, 0),
                        new EPropGroup(2,
                                new EProp(3, "id", InnerQueryConstraint.of(ConstraintOp.contains, Q4(), "P", "creationTime")))
                )).build();
        return query;
    }

    private Query Q4() {
        Query query = Query.Builder.instance().withName("q2").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "P", "Person", 2, 0),
                        new EPropGroup(2,
                                new EProp(3, "name", Constraint.of(ConstraintOp.like, "jhon*")))
                )).build();
        return query;
    }

    @Test
    public void singleNamedParameterTest() {
        AsgNamedParametersStrategy strategy = new AsgNamedParametersStrategy();
        AsgQuery query = asgSupplier.transform(Q1());
        List<String> words = Arrays.asList("Jay", "Jimmy", "Jane");
        query.setParameters(Collections.singletonList(new NamedParameter("P1.creationTime", words)));
        strategy.apply(query, new AsgStrategyContext(ont, query.getOrigin()));

        Assert.assertEquals("[└── Start, \n" +
                        "    ──Typ[:Person P1#1]]",
                AsgQueryDescriptor.print(((AsgCompositeQuery) query).getQueryChain().get(0)));

        Assert.assertTrue(AsgQueryUtil.element(query, asgEBase -> (asgEBase.geteBase() instanceof QuantBase) &&
                ((QuantBase) asgEBase.geteBase()).getqType().equals(some)).isPresent());

        AsgEBase<EBase> element = AsgQueryUtil.element(query, asgEBase -> (asgEBase.geteBase() instanceof QuantBase) &&
                ((QuantBase) asgEBase.geteBase()).getqType().equals(some)).get();
        Assert.assertEquals(element.getNext().size(),3);

        List<AsgEBase<EBase>> elements = AsgQueryUtil.elements(query, asgEBase -> (asgEBase.geteBase() instanceof EProp) &&
                ((EProp) asgEBase.geteBase()).getCon().getOp().equals(ConstraintOp.gt));
        Assert.assertEquals(elements.size(),3);
        List<String> expressions = elements.stream()
                .map(p -> ((EProp) p.geteBase()).getCon().getExpr().toString())
                .collect(Collectors.toList());
        Assert.assertTrue(expressions.containsAll(words));

    }

    @Test
    public void multipleNamedParameterTest() {
        AsgNamedParametersStrategy strategy = new AsgNamedParametersStrategy();
        AsgQuery query = asgSupplier.transform(Q2());
        List<String> words = Arrays.asList("Jay", "Jimmy", "Jane");
        query.setParameters(Collections.singletonList(new NamedParameter("P1.creationTime", words)));
        strategy.apply(query, new AsgStrategyContext(ont, query.getOrigin()));

        Assert.assertEquals("[└── Start, \n" +
                "    ──Typ[:Person P1#1]──Q[2:all]:{20}, \n" +
                "                                  └─?[20]:[Person<contains,Jimmy>]]",
                AsgQueryDescriptor.print(((AsgCompositeQuery) query).getQueryChain().get(0)));
        Assert.assertTrue(AsgQueryUtil.element(query, asgEBase -> (asgEBase.geteBase() instanceof QuantBase) &&
                ((QuantBase) asgEBase.geteBase()).getqType().equals(some)).isPresent());

        AsgEBase<EBase> element = AsgQueryUtil.element(query, asgEBase -> (asgEBase.geteBase() instanceof QuantBase) &&
                ((QuantBase) asgEBase.geteBase()).getqType().equals(some)).get();
        Assert.assertEquals(element.getNext().size(),3);

        List<AsgEBase<EBase>> elements = AsgQueryUtil.elements(query, asgEBase -> (asgEBase.geteBase() instanceof EProp) &&
                ((EProp) asgEBase.geteBase()).getCon().getOp().equals(ConstraintOp.gt));
        Assert.assertEquals(elements.size(),3);
        List<String> expressions = elements.stream()
                .map(p -> ((EProp) p.geteBase()).getCon().getExpr().toString())
                .collect(Collectors.toList());
        Assert.assertTrue(expressions.containsAll(words));
    }

    @Test
    public void innerQueryNamedParameterTest() {
        AsgNamedParametersStrategy strategy = new AsgNamedParametersStrategy();
        AsgQuery query = asgSupplier.transform(Q3());
        List<String> words = Arrays.asList("Jay", "Jimmy", "Jane");
        query.setParameters(Collections.singletonList(new NamedParameter("P.creationTime", words)));
        strategy.apply(query, new AsgStrategyContext(ont, query.getOrigin()));

        Assert.assertEquals("[└── Start, \n" +
                        "    ──Typ[:Person P#1]──?[..][2], \n" +
                        "                            └─?[3]:[name<like,jhon*>]]",
                AsgQueryDescriptor.print(((AsgCompositeQuery) query).getQueryChain().get(0)));
        Assert.assertTrue(AsgQueryUtil.element(query, asgEBase -> (asgEBase.geteBase() instanceof QuantBase) &&
                ((QuantBase) asgEBase.geteBase()).getqType().equals(some)).isPresent());

        AsgEBase<EBase> element = AsgQueryUtil.element(query, asgEBase -> (asgEBase.geteBase() instanceof QuantBase) &&
                ((QuantBase) asgEBase.geteBase()).getqType().equals(some)).get();
        Assert.assertEquals(element.getNext().size(),3);

        List<AsgEBase<EPropGroup>> elements = AsgQueryUtil.elements(query, EPropGroup.class);
        Assert.assertEquals(elements.size(),3);

        List<String> expressions = elements.stream()
                .flatMap(p -> p.geteBase().getProps().stream())
                .map(p->p.getCon().getExpr().toString())
                .collect(Collectors.toList());
        Assert.assertTrue(expressions.containsAll(words));
    }

    //region Fields
    private Ontology.Accessor ont;
    private QueryToAsgTransformer asgSupplier;

    //endregion

}