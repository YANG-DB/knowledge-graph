package org.opensearch.graph.test;

import org.opensearch.graph.client.GraphClient;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.Start;
import org.opensearch.graph.model.query.entity.EConcrete;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.opensearch.graph.model.query.quant.Quant1;
import org.opensearch.graph.model.query.quant.QuantType;
import org.opensearch.graph.model.resourceInfo.GraphResourceInfo;
import org.opensearch.graph.services.test.TestCase;

import java.util.Arrays;

import static org.opensearch.graph.model.OntologyTestUtils.*;



public class DragonKingdomQuery7Test extends TestCase {

    /**
     *
     [└── Start,
             ──Typ[Dragon:1]--> Rel(originatedIn:2)──Typ[Kingdom:3]-<--Rel(originatedIn:4)──Typ[Dragon:5]──Q[6]:{7|8|13},
                                                                                                             └─?[7]:[name<eq,BB>],
                                                                                                             └-> Rel(fire:8)──Typ[Dragon:9]--> Rel(originatedIn:10)──Typ[Kingdom:11]──?[12]:[name<eq,AB>],
                                                                                                             └-> Rel(fire:13)──Conc[Dragon:14]-<--Rel(fire:15)──Typ[Dragon:16]]
     */

    public void run(GraphClient GraphClient) throws Exception {
        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        Ontology.Accessor ont = new Ontology.Accessor(GraphClient.getOntology(graphResourceInfo.getCatalogStoreUrl() + "/Dragons"));
        Query query = Query.Builder.instance().withName(NAME.name).withOnt(ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", ont.eType$(OntologyTestUtils.DRAGON.name), 2, 0),
                new Rel(2, ont.rType$(ORIGINATED_IN.getName()), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", ont.eType$(OntologyTestUtils.KINGDOM.name), 4, 0),
                new Rel(4, ont.rType$(ORIGINATED_IN.getName()), Rel.Direction.L, null, 5, 0),
                new ETyped(5, "C", ont.eType$(OntologyTestUtils.DRAGON.name), 6, 0),
                new Quant1(6, QuantType.all,Arrays.asList(7,8,13),0),
                new EProp(7, NAME.type, Constraint.of(ConstraintOp.eq, "BB")),
                new Rel(8, ont.rType$(FIRE.getName()), Rel.Direction.R, null, 9, 0),
                new ETyped(9, "D", ont.eType$(OntologyTestUtils.DRAGON.name), 10, 0),
                new Rel(10, ont.rType$(ORIGINATED_IN.getName()), Rel.Direction.R, null, 11, 0),
                new ETyped(11, "E", ont.eType$(OntologyTestUtils.KINGDOM.name), 12, 0),
                new EProp(12, NAME.type, Constraint.of(ConstraintOp.eq, "AB")),
                new Rel(13, ont.rType$(FIRE.getName()), Rel.Direction.R, null, 14, 0),
                new EConcrete(14, "F", ont.eType$(OntologyTestUtils.DRAGON.name), "Dragon_1", "D0", 15, 0),
                new Rel(15, ont.rType$(FIRE.getName()), Rel.Direction.L, null, 16, 0),
                new ETyped(16, "B", ont.eType$(OntologyTestUtils.DRAGON.name), -1, 0)
        )).build();


        testAndAssertQuery(query, GraphClient);
    }


}
