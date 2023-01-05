package org.opensearch.graph.services.mockEngine;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.graph.client.BaseGraphClient;
import org.opensearch.graph.model.asgQuery.AsgCompositeQuery;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.execution.plan.descriptors.AsgQueryDescriptor;
import org.opensearch.graph.model.execution.plan.descriptors.QueryDescriptor;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.query.Start;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.model.transport.CreatePageRequest;
import org.opensearch.graph.model.transport.CreateQueryRequest;
import org.opensearch.graph.model.transport.PlanTraceOptions;
import org.opensearch.graph.model.transport.cursor.CreateCursorRequest;
import org.opensearch.graph.model.transport.cursor.CreatePathsCursorRequest;
import org.opensearch.graph.services.TestsConfiguration;
import org.opensearch.graph.test.BaseITMarker;
import org.opensearch.graph.test.data.DragonsOntology;
import io.restassured.http.Header;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class QueryIT implements BaseITMarker {
    @Before
    public void before() throws Exception {
//        TestSuite.setup();
        assumeTrue(TestsConfiguration.instance.shouldRunTestClass(this.getClass()));
    }

    /**
     * execute query with expected path result
     */
    @Test
    public void queryCreate() throws IOException {
        //query request
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(TestUtils.loadQuery("Q001.json"));
        //submit query
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .body(request)
                .post("/opengraph/query")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/opengraph/query/1"));
                        assertTrue(data.get("cursorStoreUrl").toString().endsWith("/opengraph/query/1/cursor"));
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");

    }

    @Test
    public void queryCreateVerbose() throws IOException {
        //query request
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(TestUtils.loadQuery("Q001.json"));
        request.setPlanTraceOptions(PlanTraceOptions.of(PlanTraceOptions.Level.verbose));
        //submit query
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .body(request)
                .post("/opengraph/query")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/opengraph/query/1"));
                        assertTrue(data.get("cursorStoreUrl").toString().endsWith("/opengraph/query/1/cursor"));
                        assertTrue(data.get("ontologyQueryUrl").toString().endsWith("/opengraph/query/1/oql"));
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");

    }

    @Test
    public void createQueryAndFetch() throws IOException {
        CreatePageRequest createPageRequest = new CreatePageRequest();
        createPageRequest.setPageSize(10);

        CreateCursorRequest createCursorRequest = new CreatePathsCursorRequest();
        createCursorRequest.setCreatePageRequest(createPageRequest);

        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(TestUtils.loadQuery("Q001.json"));
        request.setCreateCursorRequest(createCursorRequest);
        //submit query
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .body(request)
                .post("/opengraph/query")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/opengraph/query/1"));
                        assertTrue(data.get("cursorStoreUrl").toString().contains("/opengraph/query/1/cursor"));
                        assertTrue(data.get("ontologyQueryUrl").toString().endsWith("/opengraph/query/1/oql"));
                        assertTrue(((Map)(((List) data.get("cursorResourceInfos")).get(0))).containsKey("cursorRequest"));
                        assertTrue(((Map)(((List) data.get("cursorResourceInfos")).get(0))).containsKey("pageStoreUrl"));
                        assertTrue(((Map)(((List) data.get("cursorResourceInfos")).get(0))).containsKey("pageResourceInfos"));
                        assertTrue(((Map)((List)((Map)(((List) data.get("cursorResourceInfos")).get(0))).get("pageResourceInfos")).get(0)).containsKey("dataUrl"));
                        assertTrue(((Map)((List)((Map)(((List) data.get("cursorResourceInfos")).get(0))).get("pageResourceInfos")).get(0)).containsKey("actualPageSize"));
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");
    }

    @Test
    public void queryFaultyCreate() throws IOException {
        Query query = new Query();
        ETyped typed_1 = new ETyped(1, "tag", DragonsOntology.PERSON.type, 2, 0);
        ETyped typed_2 = new ETyped(2, "tag", DragonsOntology.PERSON.type, 0, 0);
        Start start = new Start(0, 1);
        query.setElements(Arrays.asList(start, typed_1, typed_2));
        query.setOnt("Dragons");
        query.setName("Q1");


        //query request
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(query);
        //submit query
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .body(request)
                .post("/opengraph/query")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl") == null);
                        assertTrue(data.get("cursorStoreUrl") == null);
                        Map errorContent = (Map) data.get("error");
                        assertTrue(errorContent.get("errorCode").toString().endsWith(Query.class.getSimpleName()));
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(500)
                .contentType("application/json;charset=UTF-8");

    }

    @Test
    public void queryCreateAndFetchResource() throws IOException {
        //query request
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(TestUtils.loadQuery("Q001.json"));
        //submit query
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .body(request)
                .post("/opengraph/query")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/opengraph/query/1"));
                        assertTrue(data.get("cursorStoreUrl").toString().endsWith("/opengraph/query/1/cursor"));
                        assertTrue(data.get("ontologyQueryUrl").toString().endsWith("/opengraph/query/1/oql"));
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");

        //get query resource by id
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .get("/opengraph/query/1")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/opengraph/query/1"));
                        assertTrue(data.get("cursorStoreUrl").toString().endsWith("/opengraph/query/1/cursor"));
                        assertTrue(data.get("ontologyQueryUrl").toString().endsWith("/opengraph/query/1/oql"));
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");

        //get cursor resource by id
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .get("/opengraph/query/1/cursor")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/opengraph/query/1/cursor"));
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");


    }

    @Test
    public void queryCreateAndFetchQueryResource() throws IOException {
        BaseGraphClient GraphClient = new BaseGraphClient("http://localhost:8888/opengraph");

        //query request
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(TestUtils.loadQuery("Q001.json"));
        //submit query
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .body(request)
                .post("/opengraph/query")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/opengraph/query/1"));
                        assertTrue(data.get("cursorStoreUrl").toString().endsWith("/opengraph/query/1/cursor"));
                        assertTrue(data.get("ontologyQueryUrl").toString().endsWith("/opengraph/query/1/oql"));
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");

        //get query resource by id
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .get("/opengraph/query/1/oql")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher((Object o) -> {
                    try {
                        Query data = GraphClient.unwrap(o.toString(), Query.class);
                        //query was augmented with auto tagging on non tagged steps
                        String actual = QueryDescriptor.print(data);
                        String expected = "[└── Start, \n" +
                                "    ──Conc[Person:1:ID[12345678]]--> Rel(own:2)──Typ[Dragon:3]]";
                        Assert.assertEquals(expected, actual);
                        return data != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");

        //get cursor resource by id
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .get("/opengraph/query/1/cursor")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/opengraph/query/1/cursor"));
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");


    }

    @Test
    public void queryCreateAndFetchAsgQueryResource() throws IOException {
        BaseGraphClient GraphClient = new BaseGraphClient("http://localhost:8888/opengraph");

        //query request
        Query query = TestUtils.loadQuery("Q001.json");
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(query);
        //submit query
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .body(request)
                .post("/opengraph/query")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/opengraph/query/1"));
                        assertTrue(data.get("cursorStoreUrl").toString().endsWith("/opengraph/query/1/cursor"));
                        assertTrue(data.get("ontologyQueryUrl").toString().endsWith("/opengraph/query/1/oql"));
                        assertTrue(data.get("asgUrl").toString().endsWith("/opengraph/query/1/asg"));
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");

        //get query resource by id
        final AsgQuery[] asgQuery = {null};
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .get("/opengraph/query/1/asg")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher((Object o) -> {
                    try {
                        asgQuery[0] = GraphClient.unwrap(o.toString(), AsgCompositeQuery.class);
                        assertTrue(asgQuery[0].getName() != null);
                        assertTrue(asgQuery[0].getOnt() != null);
                        assertTrue(AsgQueryUtil.elements(asgQuery[0]).size() >= request.getQuery().getElements().size());
                        return asgQuery[0] != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");

        //get query resource by id
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .get("/opengraph/query/1/asg/print")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher((Object o) -> {
                    try {
                        String data = StringUtils.strip(GraphClient.unwrap(o.toString()), "\"");
                        assertEquals(AsgQueryDescriptor.print(asgQuery[0]).replace("\n", "\\n"), data);
                        return data != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");

        //get cursor resource by id
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .get("/opengraph/query/1/cursor")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/opengraph/query/1/cursor"));
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");


    }

    @Test
    public void queryCreateAndDeleteResource() throws IOException {
        BaseGraphClient GraphClient = new BaseGraphClient("http://localhost:8888/opengraph");

        //query request
        Query query = TestUtils.loadQuery("Q001.json");
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(query);
        //submit query
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .body(request)
                .post("/opengraph/query")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/opengraph/query/1"));
                        assertTrue(data.get("cursorStoreUrl").toString().endsWith("/opengraph/query/1/cursor"));
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");

        //get query resource by id
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .get("/opengraph/query/1")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/opengraph/query/1"));
                        assertTrue(data.get("cursorStoreUrl").toString().endsWith("/opengraph/query/1/cursor"));
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");

        //get query resource by id
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .get("/opengraph/query/1/print")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        assertEquals(QueryDescriptor.print(query).replace("\n", "\\n"), StringUtils.strip(GraphClient.unwrap(o.toString()), "\""));
                        return GraphClient.unwrap(o.toString()) != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");

        //delete query
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .delete("/opengraph/query/1")
                .then()
                .assertThat()
                .statusCode(202)
                .contentType("application/json;charset=UTF-8");
    }

}
