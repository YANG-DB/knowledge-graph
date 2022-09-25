package org.opensearch.graph.asg.translator.cypher;

import org.opensearch.graph.asg.translator.AsgTranslator;
import org.opensearch.graph.asg.translator.cypher.strategies.MatchCypherTranslatorStrategy;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.query.QueryInfo;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collections;

import static org.opensearch.graph.model.execution.plan.descriptors.AsgQueryDescriptor.print;
import static org.opensearch.graph.model.transport.CreateQueryRequestMetadata.TYPE_CYPHERQL;
import static org.junit.Assert.assertEquals;

/**
 * Created by lior.perry
 */
public class CypherMatchMultiStatementTest {
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
    public void testMatch_2_clausesWithMultiDirections() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = " Match " +
                "   (person:Entity)-[:hasEvalue]->(personName:Evalue {stringValue:'Tom Hanks'}), " +
                "   (person:Entity)-[tomActedIn:relatedEntity {category:'ACTED_IN'}]->(m1:Entity), " +
                "   (otherPerson:Entity)-[othersActedIn:relatedEntity {category:'ACTED_IN'}]->(m2:Entity) " +
                " Where m1.name = m2.name " +
                " Return *";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));


        String expected = "[└── Start, \n" +
                            "    ──Typ[:Entity person#1]──Q[100:all]:{2|4}, \n" +
                            "                                         └-> Rel(:hasEvalue Rel_#2#2)──Typ[:Evalue personName#3]──Q[300:all]:{301}, \n" +
                            "                                                                                                              └─?[..][301]──Typ[:Entity m1#5]──Q[800:all]:{6|801}, \n" +
                            "                                                                                                                      └─?[301]:[stringValue<eq,Tom Hanks>], \n" +
                            "                                         └-> Rel(:relatedEntity tomActedIn#4), \n" +
                            "                                                                                                              └─?[..][400], \n" +
                            "                                                                                                                      └─?[402]:[category<eq,ACTED_IN>], \n" +
                            "                                                                                                                                                  └─Typ[:Entity otherPerson#6]──Q[600:all]:{7}, \n" +
                            "                                                                                                                                                                                          └-> Rel(:relatedEntity othersActedIn#7)──Typ[:Entity m2#8], \n" +
                            "                                                                                                                                                                                                                             └─?[..][700], \n" +
                            "                                                                                                                                                                                                                                     └─?[702]:[category<eq,ACTED_IN>], \n" +
                            "                                                                                                                                                  └─?[..][801], \n" +
                            "                                                                                                                                                          └─?[801]:[name<eq,m2.name>]]";
        assertEquals(expected, print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }

    @Test
    public void testMatch_2_clauses() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a:A)-[c:C]->(b:B), " +
                " (a:A)-[d:D]->(e:E)-[:F]-(g:G)" +
                " RETURN *";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        String expected = "[└── Start, \n" +
                            "    ──Typ[:A a#1]──Q[100:all]:{2|4}, \n" +
                            "                               └-> Rel(:C c#2)──Typ[:B b#3], \n" +
                            "                               └-> Rel(:D d#4)──Typ[:E e#5]──Q[500:all]:{6}, \n" +
                            "                                                                       └<--Rel(:F Rel_#6#6)──Typ[:G g#7]]";
        assertEquals(expected, print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }

    @Test
    public void testMatch_2_clauses_with_and() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a:A)-[c:C]->(b:B), " +
                " (a:A)-[d:D]->(e:E)-[:F]-(g:G)" +
                " where (b.fieldId = 'b' and b.stringValue = 'b') AND" +
                "       (e.fieldId = 'e' and e.stringValue = 'e') AND" +
                "       (g.fieldId = 'g' and g.stringValue = 'g') " +
                " RETURN *";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        String expected = "[└── Start, \n" +
                            "    ──Typ[:A a#1]──Q[100:all]:{2|4}, \n" +
                            "                               └-> Rel(:C c#2)──Typ[:B b#3]──Q[700:all]:{701}, \n" +
                            "                                                                         └─?[..][701]──Typ[:E e#5]──Q[500:all]:{6|501}, \n" +
                            "                                                                                 └─?[701]:[fieldId<eq,b>], \n" +
                            "                                                                                 └─?[702]:[stringValue<eq,b>], \n" +
                            "                               └-> Rel(:D d#4), \n" +
                            "                                          └<--Rel(:F Rel_#6#6)──Typ[:G g#7]──Q[800:all]:{801}, \n" +
                            "                                                                                         └─?[..][801], \n" +
                            "                                                                                                 └─?[801]:[stringValue<eq,g>], \n" +
                            "                                                                                                 └─?[802]:[fieldId<eq,g>], \n" +
                            "                                          └─?[..][501], \n" +
                            "                                                  └─?[501]:[fieldId<eq,e>], \n" +
                            "                                                  └─?[502]:[stringValue<eq,e>]]";
        assertEquals(expected, print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }

    @Test
    public void testMatch_2_clauses_with_or() {
        AsgTranslator<QueryInfo<String>,AsgQuery> translator = new CypherTranslator(() -> Collections.singleton(match));
        String s = "MATCH (a:A)-[c:C]->(b:B), " +
                " (a:A)-[d:D]->(e:E)-[:F]-(g:G)" +
                " where (b.fieldId = 'b' and b.stringValue = 'b') OR" +
                "       (e.fieldId = 'e' and e.stringValue = 'e') OR" +
                "       (g.fieldId = 'g' and g.stringValue = 'g') " +
                " RETURN *";
        final AsgQuery query = translator.translate(new QueryInfo<>(s,"q", TYPE_CYPHERQL, "ont"));

        String expected = "[└── Start, \n" +
                "    ──Q[700:some]:{8|15|22}, \n" +
                "                       └─Typ[:A a#8]──Q[800:all]:{9|11}, \n" +
                "                                                   └-> Rel(:C c#9)──Typ[:B b#10]──Q[1400:all]:{1401}──Q[1500:all]:{16|18}, \n" +
                "                                                                                                └─?[..][1401]──Typ[:E e#12]──Q[1200:all]:{13}──Q[2200:all]:{23|25}, \n" +
                "                                                                                                         └─?[1401]:[fieldId<eq,b>], \n" +
                "                                                                                                         └─?[1402]:[stringValue<eq,b>], \n" +
                "                                                   └-> Rel(:D d#11), \n" +
                "                                                               └<--Rel(:F Rel_#6#13)──Typ[:G g#14], \n" +
                "                       └─Typ[:A a#15], \n" +
                "                                 └-> Rel(:C c#16)──Typ[:B b#17], \n" +
                "                                 └-> Rel(:D d#18)──Typ[:E e#19]──Q[1900:all]:{20|1901}, \n" +
                "                                                                                  └<--Rel(:F Rel_#6#20)──Typ[:G g#21], \n" +
                "                                                                                  └─?[..][1901], \n" +
                "                                                                                           └─?[1901]:[fieldId<eq,e>], \n" +
                "                                                                                           └─?[1902]:[stringValue<eq,e>], \n" +
                "                       └─Typ[:A a#22], \n" +
                "                                 └-> Rel(:C c#23)──Typ[:B b#24], \n" +
                "                                 └-> Rel(:D d#25)──Typ[:E e#26]──Q[2600:all]:{27}, \n" +
                "                                                                             └<--Rel(:F Rel_#6#27)──Typ[:G g#28]──Q[2800:all]:{2801}, \n" +
                "                                                                                                                                └─?[..][2801], \n" +
                "                                                                                                                                         └─?[2801]:[stringValue<eq,g>], \n" +
                "                                                                                                                                         └─?[2802]:[fieldId<eq,g>]]";
        assertEquals(expected, print(query.withProjectedFields(Collections.EMPTY_MAP)));
    }


    //region Fields
    private MatchCypherTranslatorStrategy match;
    //endregion

}