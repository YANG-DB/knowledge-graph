package org.opensearch.graph.asg.translator.cypher;

import org.opensearch.graph.asg.translator.AsgTranslator;
import org.opensearch.graph.asg.translator.cypher.strategies.MatchCypherTranslatorStrategy;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.query.QueryInfo;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.properties.RelProp;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;

import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.execution.plan.descriptors.AsgQueryDescriptor.print;
import static org.opensearch.graph.model.query.properties.EProp.of;
import static org.opensearch.graph.model.query.properties.constraint.Constraint.of;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.eq;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.inSet;
import static org.opensearch.graph.model.query.quant.QuantType.all;
import static org.opensearch.graph.model.query.quant.QuantType.some;
import static org.opensearch.graph.model.transport.CreateQueryRequestMetadata.TYPE_CYPHERQL;
import static org.junit.Assert.assertEquals;

/**
 * Created by lior.perry
 */
public class CypherMatchHasLabelWithWhereOrOpLabelTranslatorTest {
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
    public void testMatch_A_where_A_OfType_OR_A_OfType_Return_A_with_pattern() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a {name: 'Alice'})--(b) where a:Person OR b:Dragon RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(quant1(300, some))
                .in(
                        unTyped(4, "a")
                                .addNext(
                                        quant1(400, all)
                                                .addNext(ePropGroup(400, all,
                                                        of(401, "name",of(eq,"Alice")))
                                                )
                                                .addNext(
                                                        rel(5,"*",Rel.Direction.RL,"Rel_#2")
                                                                .next(unTyped(6, "b")
                                                                        .next(quant1(600, all)
                                                                                .addNext(ePropGroup(601, all,
                                                                                        of(601, "type",
                                                                                                of(inSet, Arrays.asList("Dragon")))
                                                                                )))))
                                ),
                        unTyped(7, "a")
                                .addNext(
                                        quant1(700, all)
                                            .addNext(ePropGroup(700, all,
                                                    of(701, "name",of(eq,"Alice")),
                                                    of(701, "type",of(inSet, Arrays.asList("Person")))
                                            ))
                                            .addNext(
                                                    rel(8,"*",Rel.Direction.RL,"Rel_#2")
                                                            .next(unTyped(9, "b"))
                                            ))
                ).build();
        assertEquals(print(expected), print(query.withProjectedFields(Collections.EMPTY_MAP)));

    }
    @Test
    public void testMatch_A_where_A_OfType_B_eq_dug_OR_A_OfType_Return_A_with_pattern() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a {name: 'Alice'})--(b { size: 'large'}) where a:Person OR b:Dragon RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(quant1(300, some))
                .in(
                        unTyped(4, "a")
                                .addNext(
                                        quant1(400, all)
                                                .addNext(ePropGroup(400, all,
                                                        of(401, "name",of(eq,"Alice")))
                                                )
                                                .addNext(
                                                        rel(5,"*",Rel.Direction.RL,"Rel_#2")
                                                                .next(unTyped(6, "b")
                                                                        .next(quant1(600, all)
                                                                                .addNext(ePropGroup(600, all,
                                                                                        of(601, "size",of(eq,"large")),
                                                                                        of(601, "type",
                                                                                                of(inSet, Arrays.asList("Dragon")))
                                                                                ))
                                                                        )))
                                ),
                        unTyped(7, "a")
                                .addNext(
                                        quant1(700, all)
                                            .addNext(ePropGroup(700, all,
                                                    of(701, "name",of(eq,"Alice")),
                                                    of(701, "type",of(inSet, Arrays.asList("Person")))
                                            ))
                                            .addNext(
                                                    rel(8,"*",Rel.Direction.RL,"Rel_#2")
                                                            .next(
                                                                unTyped(9, "b")
                                                                    .next(quant1(900, all)
                                                                            .addNext(ePropGroup(900, all,
                                                                                    of(901, "size",of(eq,"large"))
                                                                            )))
                                                            )
                                            ))
                ).build();
        assertEquals(print(expected), print(query.withProjectedFields(Collections.EMPTY_MAP)));

    }

    @Test
    @Ignore
    public void testMatch_A_where_A_OfType_B_eq_C_eq_dug_OR_A_OfType_Return_A_with_pattern() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a {name: 'Alice'})-[c {color: 'white'}]-(b { size: 'large'}) where a:Person OR b:Dragon RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(quant1(300, some))
                .in(
                        unTyped(4, "a")
                                .addNext(
                                        quant1(400, all)
                                                .addNext(ePropGroup(500, all,
                                                        of(501, "name",of(eq,"Alice")))
                                                )
                                                .addNext(
                                                        rel(6,null,Rel.Direction.RL,"Rel_#2")
                                                                .next(unTyped(7, "b")
                                                                        .next(quant1(700, all)
                                                                                .addNext(ePropGroup(800, all,
                                                                                        of(801, "size",of(eq,"large")),
                                                                                        of(801, "type",
                                                                                                of(inSet, Arrays.asList("Dragon")))
                                                                                ))
                                                                        )))
                                ),
                        unTyped(8, "a")
                                .addNext(
                                        quant1(800, all)
                                            .addNext(ePropGroup(900, all,
                                                    of(901, "name",of(eq,"Alice")),
                                                    of(901, "type",of(inSet, Arrays.asList("Person")))
                                            ))
                                            .addNext(
                                                    rel(10,null,Rel.Direction.RL,"Rel_#2")
                                                            .next(
                                                                unTyped(11, "b")
                                                                    .next(quant1(1100, all)
                                                                            .addNext(ePropGroup(1200, all,
                                                                                    of(1201, "size",of(eq,"large"))
                                                                            )))
                                                            )
                                            ))
                ).build();
        assertEquals(print(expected), print(query.withProjectedFields(Collections.EMPTY_MAP)));

    }

    @Test
    public void testMatch_A_where_A_OfType_AND_A_OfType_Return_A_with_wildcard() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a) where (a.name =~ 'jh.*') OR a:Horse RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        String expected = "[└── Start, \n" +
                            "    ──Q[100:some]:{2|3}, \n" +
                            "                   └─UnTyp[:[] a#2]──Q[200:all]:{201}, \n" +
                            "                                                 └─?[..][201]──Q[300:all]:{301}, \n" +
                            "                                                         └─?[201]:[type<inSet,[Horse]>], \n" +
                            "                   └─UnTyp[:[] a#3], \n" +
                            "                               └─?[..][301], \n" +
                "                                       └─?[301]:[name<like,jh*>]]";
        assertEquals(expected, print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }

    @Test
    public void testMatch_A_where_A_OfType_OR_A_OfType_Return_A_with_containd() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a) where (a.name CONTAINS 'jh') OR a:Horse RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        String expected = "[└── Start, \n" +
                            "    ──Q[100:some]:{2|3}, \n" +
                            "                   └─UnTyp[:[] a#2]──Q[200:all]:{201}, \n" +
                            "                                                 └─?[..][201]──Q[300:all]:{301}, \n" +
                            "                                                         └─?[201]:[name<contains,jh>], \n" +
                            "                   └─UnTyp[:[] a#3], \n" +
                            "                               └─?[..][301], \n" +
                            "                                       └─?[301]:[type<inSet,[Horse]>]]";
        assertEquals(expected, print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }
    @Test
    public void testMatch_A_where_A_OfType_OR_A_OfType_Return_A_with_startsWith() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a) where (a.name STARTS WITH 'jh*') OR a:Horse RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        String expected = "[└── Start, \n" +
                "    ──Q[100:some]:{2|3}, \n" +
                "                   └─UnTyp[:[] a#2]──Q[200:all]:{201}, \n" +
                "                                                 └─?[..][201]──Q[300:all]:{301}, \n" +
                "                                                         └─?[201]:[type<inSet,[Horse]>], \n" +
                "                   └─UnTyp[:[] a#3], \n" +
                "                               └─?[..][301], \n" +
                "                                       └─?[301]:[name<startsWith,jh*>]]";
        assertEquals(expected, print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }
    @Test
    public void testMatch_A_where_A_OfType_OR_A_OfType_Return_A_with_endsWith() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a) where (a.name ENDS WITH 'jh*') OR a:Horse RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        String expected = "[└── Start, \n" +
                "    ──Q[100:some]:{2|3}, \n" +
                "                   └─UnTyp[:[] a#2]──Q[200:all]:{201}, \n" +
                "                                                 └─?[..][201]──Q[300:all]:{301}, \n" +
                "                                                         └─?[201]:[type<inSet,[Horse]>], \n" +
                "                   └─UnTyp[:[] a#3], \n" +
                "                               └─?[..][301], \n" +
                "                                       └─?[301]:[name<endsWith,jh*>]]";
        assertEquals(expected, print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }

    @Test
    public void testMatch_A_where_A_OfType_OR_A_OfType_Return_A_with_in() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a) where a.name in ['jhone','jane'] OR a:Horse RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        String expected = "[└── Start, \n" +
                            "    ──Q[100:some]:{2|3}, \n" +
                            "                   └─UnTyp[:[] a#2]──Q[200:all]:{201}, \n" +
                            "                                                 └─?[..][201]──Q[300:all]:{301}, \n" +
                            "                                                         └─?[201]:[type<inSet,[Horse]>], \n" +
                            "                   └─UnTyp[:[] a#3], \n" +
                            "                               └─?[..][301], \n" +
                            "                                       └─?[301]:[name<inSet,[jhone, jane]>]]";
        assertEquals(expected, print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }

    @Test
    public void testMatch_A_where_A_OfType_OR_A_OfType_Return_A_with_equal() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a) where a.name = 'jhone' Or a:Horse RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        String expected = "[└── Start, \n" +
                        "    ──Q[100:some]:{2|3}, \n" +
                        "                   └─UnTyp[:[] a#2]──Q[200:all]:{201}, \n" +
                        "                                                 └─?[..][201]──Q[300:all]:{301}, \n" +
                        "                                                         └─?[201]:[type<inSet,[Horse]>], \n" +
                        "                   └─UnTyp[:[] a#3], \n" +
                        "                               └─?[..][301], \n" +
                        "                                       └─?[301]:[name<eq,jhone>]]";
        assertEquals(expected, print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }

    @Test
    public void testMatch_A_where_A_OfType_OR_A_OfType_Return_A_with_notEqual() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a) where a.name <> 'jhone' Or a:Horse RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        String expected = "[└── Start, \n" +
                            "    ──Q[100:some]:{2|3}, \n" +
                            "                   └─UnTyp[:[] a#2]──Q[200:all]:{201}, \n" +
                            "                                                 └─?[..][201]──Q[300:all]:{301}, \n" +
                            "                                                         └─?[201]:[type<inSet,[Horse]>], \n" +
                            "                   └─UnTyp[:[] a#3], \n" +
                            "                               └─?[..][301], \n" +
                            "                                       └─?[301]:[name<ne,jhone>]]";
        assertEquals(expected, print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }


    @Test
    public void testMatch_A_where_A_OfType_OR_B_OfType_Return_() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a)--(b) where a:Dragon Or b:Person RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(quant1(300, some))
                .in(
                        unTyped(4, "a")
                                .next(quant1(400, all)
                                        .addNext(
                                                rel(5,"*",Rel.Direction.RL,"Rel_#2")
                                                    .next(
                                                            unTyped(6, "b")
                                                                    .next(quant1(600, all)
                                                                            .addNext(ePropGroup(601, all,
                                                                                    of(601, "type",
                                                                                            of(inSet, Arrays.asList("Person")))))))
                                                    )
                                        ),
                        unTyped(7, "a")
                                .addNext(
                                        quant1(700, all)
                                            .addNext(
                                                    rel(8,"*",Rel.Direction.RL,"Rel_#2")
                                                            .next(
                                                                    unTyped(9, "b")))
                                            .addNext(ePropGroup(701, all,
                                                    of(701, "type",
                                                        of(inSet, Arrays.asList("Dragon"))))
                                            )
                                )
                )
                .build();
        assertEquals(print(expected), print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }

    @Test
    public void testMatch_A_where_A_OfType_And_OR_B_OfType_AND_Return_() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a)-[c]-(b) where a:Dragon Or b:Person Or c:Fire RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));


        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(quant1(300, some))
                .in(
                        unTyped(4, "a")
                                .next(quant1(400, all)
                                        .addNext(
                                                rel(5,"*",Rel.Direction.RL,"c")
                                                    .next(
                                                            unTyped(6, "b")
                                                                    .next(quant1(600, all)
                                                                            .addNext(ePropGroup(601, all,
                                                                                    of(601, "type",
                                                                                            of(inSet, Arrays.asList("Person"))))))
                                                    )
                                                )
                                        ),
                        unTyped(7, "a")
                                .addNext(
                                        quant1(700, all)
                                            .addNext(
                                                    rel(8,"*",Rel.Direction.RL,"c")
                                                            .next(
                                                                    unTyped(9, "b")))
                                            .addNext(ePropGroup(701, all,
                                                    of(701, "type",
                                                        of(inSet, Arrays.asList("Dragon"))))
                                            )
                                ),
                        unTyped(10, "a")
                                .addNext(
                                        quant1(1000, all)
                                            .addNext(
                                                    rel(11,"*",Rel.Direction.RL,"c")
                                                            .below(relPropGroup(1100,all,
                                                                    RelProp.of(1101, "type", of(inSet, Arrays.asList("Fire")))))
                                                            .next(
                                                                    unTyped(12, "b")))
                                )
                ).build();
        assertEquals(print(expected), print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }

    @Test
    public void testMatch_A_AND_where_A_OfType_And_OR_B_OfType_AND_Return_() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a)-[c]-(b) where (a:Dragon AND a:Hours) Or c:Fire RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));


        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(quant1(300, some))
                .in(
                        unTyped(4, "a")
                                .addNext(
                                        quant1(400, all)
                                                .addNext(
                                                        rel(5,"*",Rel.Direction.RL,"c")
                                                                .below(relPropGroup(500,all,
                                                                        RelProp.of(501, "type", of(inSet, Arrays.asList("Fire")))))
                                                                .next(
                                                                        unTyped(6, "b")))
                                ),
                        unTyped(7, "a")
                                .addNext(
                                        quant1(700, all)
                                                .addNext(
                                                        rel(8,"*",Rel.Direction.RL,"c")
                                                                .next(
                                                                        unTyped(9, "b"))
                                                )
                                                .addNext(ePropGroup(701, all,
                                                        of(701, "type",
                                                                of(inSet, Arrays.asList("Dragon"))),
                                                        of(702, "type",
                                                                of(inSet, Arrays.asList("Hours"))))
                                                )
                                )
                ).build();
        assertEquals(print(expected), print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }

    @Test
    public void testMatch_A_AND_where_A_OfType_And_OR_B_OfType_Return_() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a)-[c]-(b) where (a:Dragon Or c:Fire) And b:Person RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));


        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(quant1(300, some))
                .in(
                        unTyped(4, "a")
                                .addNext(
                                        quant1(400, all)
                                                .addNext(
                                                        rel(5,"*",Rel.Direction.RL,"c")
                                                                .below(relPropGroup(500,all,
                                                                        RelProp.of(501, "type", of(inSet, Arrays.asList("Fire")))))
                                                                .next(
                                                                        unTyped(6, "b")
                                                                                .next(quant1(600, all)
                                                                                        .addNext(ePropGroup(601, all,
                                                                                                of(601, "type",
                                                                                                        of(inSet, Arrays.asList("Person"))))))
                                                                )
                                                )
                                ),
                        unTyped(7, "a")
                                .addNext(
                                        quant1(700, all)
                                                .addNext(
                                                        rel(8,"*",Rel.Direction.RL,"c")
                                                                .next(
                                                                        unTyped(9, "b")
                                                                                .next(quant1(900, all)
                                                                                        .addNext(ePropGroup(901, all,
                                                                                                of(901, "type",
                                                                                                        of(inSet, Arrays.asList("Person"))))))
                                                                )
                                                )
                                                .addNext(ePropGroup(701, all,
                                                        of(701, "type",
                                                                of(inSet, Arrays.asList("Dragon"))))
                                                )
                                )
                ).build();
        assertEquals(print(expected), print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }
    @Test
    public void testMatch_A_AND_where_A_OfType_And_OR_B_OfType_AND_b_Return_() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a)-[c]-(b) where ((a:Dragon And a:Hours) Or c:Fire) And b:Person RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));


        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(quant1(300, some))
                .in(
                        unTyped(4, "a")
                                .addNext(
                                        quant1(400, all)
                                                .addNext(
                                                        rel(5,"*",Rel.Direction.RL,"c")
                                                                .next(
                                                                        unTyped(6, "b")
                                                                                .next(quant1(600, all)
                                                                                        .addNext(ePropGroup(601, all,
                                                                                                of(601, "type",
                                                                                                        of(inSet, Arrays.asList("Person"))))))
                                                                )
                                                )
                                                .addNext(ePropGroup(401, all,
                                                        of(401, "type",
                                                                of(inSet, Arrays.asList("Dragon"))),
                                                        of(402, "type",
                                                                of(inSet, Arrays.asList("Hours")))
                                                        )
                                                )
                                ),
                        unTyped(7, "a")
                                .addNext(
                                        quant1(700, all)
                                                .addNext(
                                                        rel(8,"*",Rel.Direction.RL,"c")
                                                                .below(relPropGroup(800,all,
                                                                        RelProp.of(801, "type", of(inSet, Arrays.asList("Fire")))))
                                                                .next(
                                                                        unTyped(9, "b")
                                                                                .next(quant1(900, all)
                                                                                        .addNext(ePropGroup(901, all,
                                                                                                of(901, "type",
                                                                                                        of(inSet, Arrays.asList("Person"))))))
                                                                )
                                                )
                                )
                ).build();
        assertEquals(print(expected), print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }

    @Test
    public void testMatch_A_AND_where_A_OfType_And_OR_B_OfType_AND_b_OR_c_Return_() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a)-[c]-(b) where ((a:Dragon And a:Hours) Or c:Fire) And (b:Person OR b:Hours) RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        //expected string representation
        String expected = "[└── Start, \n" +
                "    ──Q[300:some]:{4|7|10|13}, \n" +
                "                         └─UnTyp[:[] a#4]──Q[400:all]:{5|401}, \n" +
                "                                                         └<--Rel(:* c#5)──UnTyp[:[] b#6]──Q[600:all]:{601}──Q[700:all]:{8}, \n" +
                "                                                                                                      └─?[..][601]──Q[1000:all]:{11}, \n" +
                "                                                                                                              └─?[601]:[type<inSet,[Person]>]──Q[1300:all]:{14|1301}, \n" +
                "                                                         └─?[..][401], \n" +
                "                                                                 └─?[401]:[type<inSet,[Dragon]>], \n" +
                "                                                                 └─?[402]:[type<inSet,[Hours]>], \n" +
                "                         └─UnTyp[:[] a#7], \n" +
                "                                     └<--Rel(:* c#8)──UnTyp[:[] b#9]──Q[900:all]:{901}, \n" +
                "                                                └─?[..][800], \n" +
                "                                                        └─?[801]:[type<inSet,[Fire]>], \n" +
                "                                                                                 └─?[..][901], \n" +
                "                                                                                         └─?[901]:[type<inSet,[Hours]>], \n" +
                "                         └─UnTyp[:[] a#10], \n" +
                "                                      └<--Rel(:* c#11)──UnTyp[:[] b#12]──Q[1200:all]:{1201}, \n" +
                "                                                  └─?[..][1100], \n" +
                "                                                           └─?[1101]:[type<inSet,[Fire]>], \n" +
                "                                                                                     └─?[..][1201], \n" +
                "                                                                                              └─?[1201]:[type<inSet,[Person]>], \n" +
                "                         └─UnTyp[:[] a#13], \n" +
                "                                      └<--Rel(:* c#14)──UnTyp[:[] b#15]──Q[1500:all]:{1501}, \n" +
                "                                                                                       └─?[..][1501], \n" +
                "                                                                                                └─?[1501]:[type<inSet,[Hours]>], \n" +
                "                                      └─?[..][1301], \n" +
                "                                               └─?[1301]:[type<inSet,[Dragon]>], \n" +
                "                                               └─?[1302]:[type<inSet,[Hours]>]]";
        assertEquals(expected, print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }

    @Test
    @Ignore
    //todo add Rule for same variable with multiple operators
    public void testMatch_A_OR_where_A_OfType_And_OR_B_OfType_AND_Return_() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a)-[c]-(b) where (a:Dragon OR a:Hours) Or c:Fire RETURN a";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));


        AsgQuery expected = AsgQuery.Builder
                .start("cypher_", "Dragons")
                .next(quant1(300, some))
                .in(
                        unTyped(4, "a")
                                .addNext(
                                        quant1(400, all)
                                                .addNext(
                                                        rel(6,"*",Rel.Direction.RL,"c")
                                                                .below(relPropGroup(600,all,
                                                                        RelProp.of(60100, "type", of(inSet, Arrays.asList("Fire")))))
                                                                .next(
                                                                        unTyped(7, "b")))
                                ),
                        unTyped(8, "a")
                                .addNext(
                                        quant1(800, all)
                                                .addNext(
                                                        rel(10,"*",Rel.Direction.RL,"c")
                                                                .next(
                                                                        unTyped(11, "b"))
                                                )
                                                .addNext(ePropGroup(801, some,
                                                        of(801, "type",
                                                                of(inSet, Arrays.asList("Hours"))),
                                                        of(802, "type",
                                                                of(inSet, Arrays.asList("Dragon"))))
                                                )
                                )
                ).build();
        assertEquals(print(expected), print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }



    //region Fields
    private MatchCypherTranslatorStrategy match;
    //endregion

}