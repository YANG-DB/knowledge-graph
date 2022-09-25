package org.opensearch.graph.test;

import org.opensearch.graph.client.GraphClient;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.Start;
import org.opensearch.graph.model.query.entity.EConcrete;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.resourceInfo.GraphResourceInfo;
import org.opensearch.graph.services.test.TestCase;

import java.util.Arrays;

import static org.opensearch.graph.model.OntologyTestUtils.*;

public class DragonKingdomQuery4Test extends TestCase {

    public void run(GraphClient GraphClient) throws Exception {
        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        Ontology.Accessor ont = new Ontology.Accessor(GraphClient.getOntology(graphResourceInfo.getCatalogStoreUrl() + "/Dragons"));
        Query query = Query.Builder.instance().withName(NAME.name).withOnt(ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new EConcrete(1, "A", ont.eType$(OntologyTestUtils.DRAGON.name), "Dragon_100", "D0", 2, 0),
                new Rel(2, ont.rType$(FIRE.getName()), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", ont.eType$(OntologyTestUtils.DRAGON.name), 4, 0),
                new Rel(4, ont.rType$(ORIGINATED_IN.getName()), Rel.Direction.R, null, 5, 0),
                new ETyped(5, "C", ont.eType$(OntologyTestUtils.KINGDOM.name), 6, 0),
                new Rel(6, ont.rType$(ORIGINATED_IN.getName()), Rel.Direction.L, null, 7, 0),
                new ETyped(7, "D", ont.eType$(OntologyTestUtils.DRAGON.name), 8, 0),
                new Rel(8, ont.rType$(FIRE.getName()), Rel.Direction.L, null, 9, 0),
                new EConcrete(9, "E", ont.eType$(OntologyTestUtils.DRAGON.name), "Dragon_200", "D0", 0, 0)
        )).build();
        /*Query query = Query.Builder.instance().withName(NAME.name).withOnt(ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new EConcrete(1, "A", ont.eType$(OntologyTestUtils.DRAGON.name), "Dragon_100", "D0", singletonList(NAME.type), 2, 0),
                new Rel(2, ont.rType$(FIRE.getTyped()), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", ont.eType$(OntologyTestUtils.DRAGON.name), singletonList(NAME.type), 0, 0)
        )).build();*/


        testAndAssertQuery(query, GraphClient);
    }


}
