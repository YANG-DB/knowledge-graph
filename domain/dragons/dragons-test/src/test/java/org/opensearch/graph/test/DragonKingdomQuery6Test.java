package org.opensearch.graph.test;

import org.opensearch.graph.client.GraphClient;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.Start;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.opensearch.graph.model.resourceInfo.GraphResourceInfo;
import org.opensearch.graph.services.test.TestCase;

import java.util.Arrays;

import static org.opensearch.graph.model.OntologyTestUtils.NAME;
import static org.opensearch.graph.model.OntologyTestUtils.ORIGINATED_IN;

/**
 * Created by lior.perry on 27/02/2018.
 */
public class DragonKingdomQuery6Test extends TestCase{
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
                new EProp(6, NAME.type, Constraint.of(ConstraintOp.eq, "D"))

        )).build();


        testAndAssertQuery(query, GraphClient);
    }
}
