package org.opensearch.graph.asg.strategy.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.graph.dispatcher.asg.QueryToAsgTransformer;
import org.opensearch.graph.dispatcher.ontology.SimpleOntologyMappingProvider;
import org.opensearch.graph.dispatcher.ontology.InMemoryOntologyProvider;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.execution.plan.descriptors.AsgQueryDescriptor;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.mapping.MappingOntologies;
import org.opensearch.graph.model.query.entity.TypedEndPattern;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.RelProp;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;

import static org.opensearch.graph.dispatcher.utils.GraphApiUtils.findPathQuery;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.query.Rel.Direction.L;
import static org.opensearch.graph.model.query.Rel.Direction.R;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.eq;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.gt;
import static org.opensearch.graph.model.query.quant.QuantType.all;

public class AsgMappingStrategyTest extends TestCase {
    private ObjectMapper mapper;
    private AsgMappingStrategy strategy;
    private QueryToAsgTransformer asgTransformer;

    @Override
    public void setUp() throws Exception {
        InputStream logicalDragonsSource = Thread.currentThread().getContextClassLoader().getResourceAsStream("mapping/LogicalDragons.json");
        InputStream schemaSource = Thread.currentThread().getContextClassLoader().getResourceAsStream("mapping/Dragons.json");
        InputStream mappingSource = Thread.currentThread().getContextClassLoader().getResourceAsStream("mapping/mapping.json");

        mapper = new ObjectMapper();
        Ontology ontology = mapper.readValue(logicalDragonsSource, Ontology.class);
        Ontology schema = mapper.readValue(schemaSource, Ontology.class);
        MappingOntologies mapping = mapper.readValue(mappingSource, MappingOntologies.class);

        asgTransformer = new QueryToAsgTransformer();
        strategy = new AsgMappingStrategy(new InMemoryOntologyProvider(ontology, schema), new SimpleOntologyMappingProvider(mapping));
    }

    @Test
    public void testQ1TransformMapping() {
        AsgQuery query = Q1();
        strategy.apply(query, null);
        Assert.assertEquals("[└── Start, \n" +
                "    ──Typ[:Entity A#1]──Q[2:all]:{3}, \n" +
                "                                └─?[..][3], \n" +
                "                                      └─?[3]:[name<like,a>], \n" +
                "                                      └─?[3]:[Age<gt,13>], \n" +
                "                                      └─?[3]:[type<eq,0>]]",
                AsgQueryDescriptor.print(query));
    }

    @Test
    public void testQ2TransformMapping() {
        AsgQuery query = Q2();
        strategy.apply(query, null);
        Assert.assertEquals("[└── Start, \n" +
                "    ──Typ[:Entity A#1]──Q[2:all]:{3}, \n" +
                "                                └─?[..][3], \n" +
                "                                      └─?[3]:[type<eq,0>]]", AsgQueryDescriptor.print(query));
    }

    @Test
    public void testQ3TransformMapping() {
        AsgQuery query = Q3();
        strategy.apply(query, null);
        Assert.assertEquals("[└── Start, \n" +
                        "    ──Typ[:Entity A#1]──Q[17:all]:{2|18}, \n" +
                        "                                    └-> Rel(:Relationship R#2)──Typ[:Entity B#3]──Q[4:all]:{5|6|11}, \n" +
                        "                                                          └─?[..][2], \n" +
                        "                                                                └─?[2]:[2<eq,value2>], \n" +
                        "                                                                └─?[2]:[type<eq,101>], \n" +
                        "                                                                                 └─?[..][5], \n" +
                        "                                                                                       └─?[5]:[prop1<eq,value1>]──Typ[:Entity C#7]──Q[19:all]:{20}, \n" +
                        "                                                                                       └─?[5]:[prop2<gt,value3>], \n" +
                        "                                                                                       └─?[5]:[type<eq,0>], \n" +
                        "                                                                                 └-> Rel(:Relationship R1#6), \n" +
                        "                                                                                       └─?[..][25], \n" +
                        "                                                                                              └─?[25]:[type<eq,100>], \n" +
                        "                                                                                                                └─?[..][20], \n" +
                        "                                                                                                                       └─?[20]:[type<eq,2>], \n" +
                        "                                                                                 └─AsgEBase[11], \n" +
                        "                                    └─?[..][18], \n" +
                        "                                           └─?[18]:[type<eq,3>]]",
                AsgQueryDescriptor.print(query));
        assertNotNull(AsgQueryDescriptor.printGraph(query));
    }

//    @Test //todo FIXME
    @Ignore("FIXME: 22/06/2021")
    public void FIXME_testQ4TransformMapping() {
        AsgQuery query = Q4();
        strategy.apply(query, null);
        Assert.assertEquals("[└── Start, \n" +
                        "    ──Conc[:Entity Dragon_d01#1]──Q[4:all]:{2|5}, \n" +
                        "                                            └-> Rel(:Relationship null#2)──Typ[:Entity Person$:{}#3]──Q[6:all]:{7}, \n" +
                        "                                                                     └─?[..][8], \n" +
                        "                                                                           └─?[8]:[type<eq,101>], \n" +
                        "                                                                                            └─?[..][7], \n" +
                        "                                                                                                  └─?[7]:[type<eq,0>], \n" +
                        "                                            └─?[..][5], \n" +
                        "                                                  └─?[5]:[type<eq,3>]]",
                AsgQueryDescriptor.print(query));

        Assert.assertTrue(AsgQueryUtil.element(query, TypedEndPattern.class).isPresent());
        //verify type was mapped inside the EndPattern fields as well
        Assert.assertEquals("Entity", AsgQueryUtil.element(query, TypedEndPattern.class).get().geteBase().getEndEntity().geteType());
    }


    private AsgQuery Q1() {
        return AsgQuery.Builder.start("Q2", "LogicalDragons")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "a")),
                        EProp.of(3, "Age", Constraint.of(gt, 13))))
                .build();
    }

    private AsgQuery Q2() {
        return AsgQuery.Builder.start("Q2", "LogicalDragons")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "type", Constraint.of(ConstraintOp.like, "a*"))))
                .build();
    }
    private AsgQuery Q4() {
        return asgTransformer.transform(
                findPathQuery("LogicalDragons","Dragon","d01","Person","p01","Fire",3));
    }

    private AsgQuery Q3() {
        return AsgQuery.Builder.start("Q4", "LogicalDragons")
                .next(typed(1, "Dragon", "A"))
                .next(rel(2, "Fire", R, "R")
                        .below(relProp(2, RelProp.of(2, "2", Constraint.of(eq, "value2")))))
                .next(typed(3, "Person", "B"))
                .next(quant1(4, all))
                .in(ePropGroup(5, EProp.of(5, "prop1", Constraint.of(eq, "value1")),
                                        EProp.of(5, "prop2", Constraint.of(gt, "value3"))),
                        rel(6, "Own", R, "R1").next(
                                typed(7, "Horse", "C")),
                        optional(11)
                                .next(rel(12, "OriginatedIn", R, "R2")
                                .next(typed(13, "Kingdom", "E")
                                .next(optional(14)
                                        .next(rel(15, "RegisteredIn", L, "R2").
                                                next(typed(16, "Guild", "F")))))))
                .build();
    }


}
