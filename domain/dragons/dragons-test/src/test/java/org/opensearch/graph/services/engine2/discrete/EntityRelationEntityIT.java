package org.opensearch.graph.services.engine2.discrete;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.action.admin.indices.refresh.RefreshRequest;
import org.opensearch.client.transport.TransportClient;
import org.opensearch.graph.client.BaseGraphClient;
import org.opensearch.graph.client.GraphClient;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.Start;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.opensearch.graph.model.query.quant.Quant1;
import org.opensearch.graph.model.query.quant.QuantType;
import org.opensearch.graph.model.resourceInfo.CursorResourceInfo;
import org.opensearch.graph.model.resourceInfo.GraphResourceInfo;
import org.opensearch.graph.model.resourceInfo.PageResourceInfo;
import org.opensearch.graph.model.resourceInfo.QueryResourceInfo;
import org.opensearch.graph.model.results.AssignmentsQueryResult;
import org.opensearch.graph.model.transport.cursor.CreateGraphCursorRequest;
import org.opensearch.graph.test.BaseITMarker;
import org.opensearch.graph.test.framework.index.MappingEngineConfigurer;
import org.opensearch.graph.test.framework.index.Mappings;
import org.opensearch.graph.test.framework.index.Mappings.Mapping;
import org.opensearch.graph.test.framework.index.SearchEmbeddedNode;
import org.opensearch.graph.test.framework.populator.SearchEngineDataPopulator;

import java.io.IOException;
import java.util.*;

import static org.opensearch.graph.model.OntologyTestUtils.*;
import static org.opensearch.graph.test.framework.index.Mappings.Mapping.Property.Type.keyword;

/**
 * Created by roman.margolis on 02/10/2017.
 */
public class EntityRelationEntityIT implements BaseITMarker {
    //region setup
    @BeforeClass
    public static void setup() throws Exception {
        GraphClient = new BaseGraphClient("http://localhost:8888/opengraph");

        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        $ont = new Ontology.Accessor(GraphClient.getOntology(graphResourceInfo.getCatalogStoreUrl() + "/Dragons"));

        String idField = "id";

        TransportClient client = SearchEmbeddedNode.getClient();

        new MappingEngineConfigurer(Arrays.asList("person1", "person2"), new Mappings().addMapping("pge",
                new Mapping().addProperty("type", new Mapping.Property(keyword))
                        .addProperty("name", new Mapping.Property(keyword)))).configure(client);

        new SearchEngineDataPopulator(
                client,
                "person1",
                "pge",
                idField,
                true,
                null,
                false,
                () -> createPeople(0, 5)).populate();

        new SearchEngineDataPopulator(
                client,
                "person2",
                "pge",
                idField,
                true,
                null,
                false,
                () -> createPeople(5, 10)).populate();

        new MappingEngineConfigurer(Arrays.asList("dragon1", "dragon2"), new Mappings().addMapping("pge",
                new Mapping().addProperty("type", new Mapping.Property(keyword))
                        .addProperty("name", new Mapping.Property(keyword))
                        .addProperty("personId", new Mapping.Property(keyword)))).configure(client);

        new SearchEngineDataPopulator(
                client,
                "dragon1",
                "pge",
                idField,
                true,
                "personId",
                false,
                () -> createDragons(0, 5, 3)).populate();

        new SearchEngineDataPopulator(
                client,
                "dragon2",
                "pge",
                idField,
                true,
                "personId",
                false,
                () -> createDragons(5, 10, 3)).populate();

        client.admin().indices().refresh(new RefreshRequest("person1", "person2", "dragon1", "dragon2")).actionGet();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        SearchEmbeddedNode.getClient().admin().indices()
                .delete(new DeleteIndexRequest("person1", "person2", "dragon1", "dragon2")).actionGet();
    }
    //endregion

    //region Tests
    @Test
    public void test_Person_own_Dragon_paths() throws IOException, InterruptedException {
        Query query = Query.Builder.instance().withName("q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", PERSON.type, 2, 0),
                new Rel(2, OWN.getrType(), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", DRAGON.type, 0, 0)
        )).build();

        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }

    @Test
    public void test_Person_own_Dragon_graph() throws IOException, InterruptedException {
        Query query = Query.Builder.instance().withName("q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", PERSON.type, 2, 0),
                new Rel(2, OWN.getrType(), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", DRAGON.type, 0, 0)
        )).build();

        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(
                queryResourceInfo.getCursorStoreUrl(),
                new CreateGraphCursorRequest());
        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }

    @Test
    public void test_person1_own_Dragon_paths() throws IOException, InterruptedException {
        Query query = Query.Builder.instance().withName("q1").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", PERSON.type, 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4), 0),
                new EProp(3, NAME.type, Constraint.of(ConstraintOp.eq, "person1")),
                new Rel(4, OWN.getrType(), Rel.Direction.R, null, 5, 0),
                new ETyped(5, "B", DRAGON.type, 0, 0)
        )).build();

        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());
        int x = 5;
    }
    //endregion

    //region Protected Methods
    protected static Iterable<Map<String, Object>> createPeople(int startId, int endId) {
        List<Map<String, Object>> people = new ArrayList<>();
        for(int i = startId ; i < endId ; i++) {
            Map<String, Object> person = new HashMap<>();
            person.put("id", "p" + String.format("%03d", i));
            person.put("type", "Person");
            person.put("name", "person" + i);
            people.add(person);
        }
        return people;
    }

    protected static Iterable<Map<String, Object>> createDragons(int personStartId, int personEndId, int numDragonsPerPerson) {
        int dragonId = personStartId * numDragonsPerPerson;

        List<Map<String, Object>> dragons = new ArrayList<>();
        for(int i = personStartId ; i < personEndId ; i++) {
            for (int j = 0; j < numDragonsPerPerson; j++) {
                Map<String, Object> dragon = new HashMap<>();
                dragon.put("id", "d" + String.format("%03d", dragonId));
                dragon.put("type", "Dragon");
                dragon.put("personId", "p" + String.format("%03d", i));
                dragon.put("name", "dragon" + dragonId);
                dragons.add(dragon);

                dragonId++;
            }
        }

        return dragons;
    }
    //endregion

    //region Fields
    private static GraphClient GraphClient;
    private static Ontology.Accessor $ont;
    //endregion
}
