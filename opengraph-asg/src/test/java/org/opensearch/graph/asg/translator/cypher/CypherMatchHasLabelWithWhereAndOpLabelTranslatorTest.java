package org.opensearch.graph.asg.translator.cypher;

import org.opensearch.graph.asg.translator.AsgTranslator;
import org.opensearch.graph.asg.translator.cypher.strategies.MatchCypherTranslatorStrategy;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.query.QueryInfo;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.properties.RelProp;
import org.opensearch.graph.model.query.quant.Quant1;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;

import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.execution.plan.descriptors.AsgQueryDescriptor.print;
import static org.opensearch.graph.model.query.properties.EProp.of;
import static org.opensearch.graph.model.query.properties.constraint.Constraint.of;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.*;
import static org.opensearch.graph.model.query.quant.QuantType.all;
import static org.opensearch.graph.model.transport.CreateQueryRequestMetadata.TYPE_CYPHERQL;
import static org.junit.Assert.assertEquals;

/**
 * Created by lior.perry
 */
public class CypherMatchHasLabelWithWhereAndOpLabelTranslatorTest {
    //region Setup
    @Before
    public void setUp() throws Exception {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("Dragons_Ontology.json");
        StringWriter writer = new StringWriter();
        IOUtils.copy(stream, writer);
        match = new CypherTestUtils().setUp(writer.toString()).match;
    }
    //endregion

    @Test
    public void testMatch_A_where_A_OfType_AND_A_OfType_Return_A_with_pattern() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a {name: 'Alice'}) where a:Horse RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        String expected = "[└── Start, \n" +
                "    ──UnTyp[:[] a#1]──Q[100:all]:{101}, \n" +
                "                                  └─?[..][101], \n" +
                "                                          └─?[101]:[name<eq,Alice>], \n" +
                "                                          └─?[102]:[type<inSet,[Horse]>]]";
        assertEquals(expected, print(query.withProjectedFields(Collections.EMPTY_MAP)));

    }
    @Test
    public void testMatch_A_where_A_OfType_AND_A_OfType_Return_A_with_multi_pattern() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a {name: 'Alice'})--(b {age: 30}) where a:Horse RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        final AsgEBase<Quant1> quantA = quant1(100, all);

        quantA.addNext(
                ePropGroup(101,all,
                        of(101, "name", of(eq, "Alice")),
                        of(102, "type", of(inSet, Collections.singleton("Horse")))));

        quantA.addNext(
                rel(2, "*", Rel.Direction.RL,"Rel_#2")
                    .addNext(unTyped(3, "b")
                        .next(quant1(300, all)
                                .addNext(
                                        ePropGroup(301,all,
                                                of(301, "age", of(eq, 30)))
                                )
                        )));

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .next(quantA)
                .build();
        assertEquals(print(expected), print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }
    @Test
    public void testMatch_A_where_A_OfType_AND_A_OfType_AND_B_OfType_Return_A_with_multi_pattern() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a {name: 'Alice'})--(b {age: 30}) where a:Horse and b:Dragon RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        final AsgEBase<Quant1> quantA = quant1(100, all);

        quantA.addNext(
                ePropGroup(101,all,
                        of(101, "name", of(eq, "Alice")),
                        of(102, "type", of(inSet, Collections.singletonList("Horse")))));

        quantA.addNext(
                rel(2, "*", Rel.Direction.RL,"Rel_#2")
                    .addNext(unTyped(3, "b")
                        .next(quant1(300, all)
                                .addNext(
                                        ePropGroup(301,all,
                                                of(301, "age", of(eq, 30)),
                                                of(302, "type", of(inSet, Collections.singletonList("Dragon"))))
                                )
                        )));

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .next(quantA)
                .build();
        assertEquals(print(expected), print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }

    @Test
    public void testMatch_A_where_A_OfType_AND_A_OfType_Return_A_with_wildcard() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a) where (a.name =~ 'jh.*') AND a:Horse RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .next(quant1(100, all))
                .in(
                        ePropGroup(101,all,
                                of(101, "type", of(inSet, Arrays.asList("Horse"))),
                                of(102, "name", of(like, "jh*"))
                        )
                ).build();
        assertEquals(print(expected), print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }
    @Test
    public void testMatch_A_where_A_OfType_AND_A_OfType_Return_A_with_contains() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a) where (a.name CONTAINS 'jh') AND a:Horse RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .next(quant1(100, all))
                .in(
                        ePropGroup(101,all,
                                of(101, "name", of(contains, "jh")),
                                of(102, "type", of(inSet, Arrays.asList("Horse")))
                        )
                ).build();
        assertEquals(print(expected), print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }
    @Test
    public void testMatch_A_where_A_OfType_AND_A_OfType_Return_A_with_endsWith() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a) where (a.name ENDS WITH 'jh*') AND a:Horse RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .next(quant1(100, all))
                .in(
                        ePropGroup(101,all,
                                of(101, "type", of(inSet, Arrays.asList("Horse"))),
                                of(102, "name", of(endsWith, "jh*")))
                )
                .build();
        assertEquals(print(expected), print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }

    @Test
    public void testMatch_A_where_A_OfType_AND_A_OfType_Return_A_with_in() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a) where (a.name STARTS WITH 'jh*')  AND a:Horse RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .next(quant1(100, all))
                .in(
                        ePropGroup(101,all,
                                of(101, "type", of(inSet, Arrays.asList("Horse"))),
                                of(102, "name", of(startsWith, "jh*")))
                )
                .build();
        assertEquals(print(expected), print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }


    @Test
    public void testMatch_A_where_A_OfType_Return_A() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a) where a:Dragon RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .next(quant1(100, all))
                .in(ePropGroup(101,all,of(101, "type", of(inSet, Arrays.asList("Dragon")))))
                .build();
        assertEquals(print(expected), print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }

    @Test
    public void testMatch_A_where_A_OfType_OR_A_OfType_Return_A() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a) where a:Dragon AND a:Hours RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .next(quant1(100, all))
                .in(
                        ePropGroup(101,all,
                                of(101, "type", of(inSet, Arrays.asList("Dragon"))),
                                of(102, "type", of(inSet, Arrays.asList("Hours"))))
                )
                .build();
        assertEquals(print(expected), print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }

    @Test
    public void testMatch_A_where_A_OfType_OR_A_OfType_Return_A_with_in() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a) where a.name in ['jhone','jane'] AND a:Horse RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .next(quant1(100, all))
                .in(
                        ePropGroup(101,all,
                                of(101, "type", of(inSet, Arrays.asList("Horse"))),
                                of(102, "name", of(inSet, Arrays.asList("jhone","jane"))))
                )
                .build();
        assertEquals(print(expected), print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }

    @Test
    public void testMatch_A_where_A_OfType_OR_A_OfType_Return_A_with_equal() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a) where a.name = 'jhone' AND a:Horse RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .next(quant1(100, all))
                .in(
                        ePropGroup(101,all,
                                of(101, "type", of(inSet, Arrays.asList("Horse"))),
                                of(102, "name", of(eq, "jhone")))
                )
                .build();
        assertEquals(print(expected), print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }

    @Test
    public void testMatch_A_where_A_OfType_OR_A_OfType_Return_A_with_notEqual() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a) where a.name <> 'jhone' AND a:Horse RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .next(quant1(100, all))
                .in(
                        ePropGroup(101,all,
                                of(101, "type", of(inSet, Arrays.asList("Horse"))),
                                of(102, "name", of(ne, "jhone")))
                )
                .build();
        assertEquals(print(expected), print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }

    @Test
    public void testMatch_A_where_A_OfType_AND_A_OfType_Return_A() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a)--(b) where a:Dragon AND a:Hours AND b:Person RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        final AsgEBase<Quant1> quantA = quant1(100, all);
        quantA.addNext(rel(2, "*", Rel.Direction.RL,"Rel_#2")
                .addNext(unTyped(3, "b")
                        .next(quant1(300, all)
                                .addNext(
                                        ePropGroup(301,all,
                                                of(301, "type", of(inSet, Arrays.asList("Person"))))
                                )
                        )));
        quantA.addNext(
                ePropGroup(101,all,
                        of(101, "type", of(inSet, Arrays.asList("Dragon"))),
                        of(102, "type", of(inSet, Arrays.asList("Hours")))));

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .next(quantA)
                .build();
        assertEquals(print(expected), print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }

    @Test
    public void testMatch_A_where_A_OfType_AND_B_OfType_Return_A() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a)--(b) where a:Dragon AND b:Person RETURN a,b";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));


        //region Test Methods

        final AsgEBase<Quant1> quantA = quant1(100, all);
        quantA.addNext(rel(2, "*", Rel.Direction.RL,"Rel_#2")
                .addNext(unTyped(3, "b")
                        .next(quant1(300, all)
                                .addNext(
                                        ePropGroup(301,all,
                                            of(301, "type", of(inSet, Arrays.asList("Person"))))
                        )
                )));
        quantA.addNext(
                ePropGroup(101,all,
                    of(101, "type", of(inSet, Arrays.asList("Dragon")))));

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .next(quantA)
                .build();
        assertEquals(print(expected), print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }
    @Test
    public void testMatch_A_where_A_OfType_testMatch_A_where_A_OfType_AND_C_OfType_Return_A() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a)-[c]-(b) where a:Dragon AND b:Person AND c:Fire AND c:Freeze RETURN a,b";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));


        //region Test Methods

        final AsgEBase<Quant1> quantA = quant1(100, all);
        quantA.addNext(rel(2, "*", Rel.Direction.RL,"c")
                .below(relPropGroup(200,all,
                        new RelProp(201,"type",of(inSet, Arrays.asList("Freeze")),0),
                        new RelProp(201,"type",of(inSet, Arrays.asList("Fire")),0)))
                .addNext(unTyped(3, "b")
                        .next(quant1(300, all)
                                .addNext(
                                        ePropGroup(301,all,
                                            of(301, "type", of(inSet, Arrays.asList("Person")))))
                        )

                ));
        quantA.addNext(
                ePropGroup(101,all,
                    of(101, "type", of(inSet, Arrays.asList("Dragon")))));

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(unTyped(1, "a"))
                .next(quantA)
                .build();
        assertEquals(print(expected), print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }

    //endregion

    //region Fields
    private MatchCypherTranslatorStrategy match;
    //endregion

}