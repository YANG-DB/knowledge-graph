package org.opensearch.graph.services.mockEngine;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.graph.client.BaseGraphClient;
import org.opensearch.graph.model.execution.plan.descriptors.QueryDescriptor;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.query.Start;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.resourceInfo.QueryResourceInfo;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.model.transport.CreatePageRequest;
import org.opensearch.graph.model.transport.CreateQueryRequest;
import org.opensearch.graph.model.transport.cursor.CreateCursorRequest;
import org.opensearch.graph.model.transport.cursor.CreatePathsCursorRequest;
import org.opensearch.graph.test.BaseITMarker;
import org.opensearch.graph.test.data.DragonsOntology;
import io.restassured.http.Header;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QueryCompositeIT implements BaseITMarker {
    @Before
    public void before() throws Exception {
        TestSuiteAPISuite.setup();
//        Assume.assumeTrue(TestsConfiguration.instance.shouldRunTestClass(this.getClass()));
    }

    /**
     * execute query with expected path result
     */
    @Test
    public void queryWithOneInnerQueryCreate() throws IOException {
        BaseGraphClient GraphClient = new BaseGraphClient("http://localhost:8888/fuse");
        //query request
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(CompositeQueryTestUtils.Q1());
        //submit query
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .body(request)
                .post("/fuse/query")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        final QueryResourceInfo queryResourceInfo = GraphClient.unwrap(o.toString(), QueryResourceInfo.class);
                        assertTrue(queryResourceInfo.getAsgUrl().endsWith("fuse/query/1/asg"));
                        assertTrue(queryResourceInfo.getCursorStoreUrl().endsWith("fuse/query/1/cursor"));
                        assertEquals(1,queryResourceInfo.getInnerUrlResourceInfos().size());
                        assertTrue(queryResourceInfo.getInnerUrlResourceInfos().get(0).getAsgUrl().endsWith("fuse/query/1->q2/asg"));
                        assertTrue(queryResourceInfo.getInnerUrlResourceInfos().get(0).getCursorStoreUrl().endsWith("fuse/query/1->q2/cursor"));
                        return GraphClient.unwrap(o.toString()) != null;
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
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/"+request.getId()+"->"+ CompositeQueryTestUtils.Q2().getName())
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        final QueryResourceInfo queryResourceInfo = GraphClient.unwrap(o.toString(), QueryResourceInfo.class);
                        assertTrue(queryResourceInfo.getAsgUrl().endsWith("fuse/query/1->q2/asg"));
                        assertTrue(queryResourceInfo.getCursorStoreUrl().endsWith("fuse/query/1->q2/cursor"));
                        return GraphClient.unwrap(o.toString()) != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");


    }

    @Test
    @Ignore("Only single hierarchy level if allowed")
    public void queryWithHierarchyInnerQueryCreate() throws IOException {
        BaseGraphClient GraphClient = new BaseGraphClient("http://localhost:8888/fuse");
        //query request
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(CompositeQueryTestUtils.Q0());
        //submit query
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .body(request)
                .post("/fuse/query")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        final QueryResourceInfo queryResourceInfo = GraphClient.unwrap(o.toString(), QueryResourceInfo.class);
                        assertTrue(queryResourceInfo.getAsgUrl().endsWith("fuse/query/1/asg"));
                        assertTrue(queryResourceInfo.getCursorStoreUrl().endsWith("fuse/query/1/cursor"));
                        assertEquals(1,queryResourceInfo.getInnerUrlResourceInfos().size());
                        assertTrue(queryResourceInfo.getInnerUrlResourceInfos().get(0).getAsgUrl().endsWith("fuse/query/1->q1/asg"));
                        assertTrue(queryResourceInfo.getInnerUrlResourceInfos().get(0).getCursorStoreUrl().endsWith("fuse/query/1->q1/cursor"));
                        return GraphClient.unwrap(o.toString()) != null;
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
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/"+request.getId()+"->"+ CompositeQueryTestUtils.Q1().getName())
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        final QueryResourceInfo queryResourceInfo = GraphClient.unwrap(o.toString(), QueryResourceInfo.class);
                        assertTrue(queryResourceInfo.getAsgUrl().endsWith("fuse/query/1->q1/asg"));
                        assertTrue(queryResourceInfo.getCursorStoreUrl().endsWith("fuse/query/1->q1/cursor"));
                        assertEquals(1,queryResourceInfo.getInnerUrlResourceInfos().size());
                        assertTrue(queryResourceInfo.getInnerUrlResourceInfos().get(0).getAsgUrl().endsWith("fuse/query/1->q2/asg"));
                        assertTrue(queryResourceInfo.getInnerUrlResourceInfos().get(0).getCursorStoreUrl().endsWith("fuse/query/1->q2/cursor"));
                        return GraphClient.unwrap(o.toString()) != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");

        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/"+request.getId()+"->"+ CompositeQueryTestUtils.Q2().getName())
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        final QueryResourceInfo queryResourceInfo = GraphClient.unwrap(o.toString(), QueryResourceInfo.class);
                        assertTrue(queryResourceInfo.getAsgUrl().endsWith("fuse/query/1->q2/asg"));
                        assertTrue(queryResourceInfo.getCursorStoreUrl().endsWith("fuse/query/1->q2/cursor"));
                        assertEquals(0,queryResourceInfo.getInnerUrlResourceInfos().size());
                        return GraphClient.unwrap(o.toString()) != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");


    }

    @Test
    public void queryWithTwoInnerQueryCreate() throws IOException {
        BaseGraphClient GraphClient = new BaseGraphClient("http://localhost:8888/fuse");
        //query request
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(CompositeQueryTestUtils.Q4());
        //submit query
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .body(request)
                .post("/fuse/query")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        final QueryResourceInfo queryResourceInfo = GraphClient.unwrap(o.toString(), QueryResourceInfo.class);
                        assertTrue(queryResourceInfo.getAsgUrl().endsWith("fuse/query/1/asg"));
                        assertTrue(queryResourceInfo.getCursorStoreUrl().endsWith("fuse/query/1/cursor"));
                        assertEquals(2,queryResourceInfo.getInnerUrlResourceInfos().size());
                        return GraphClient.unwrap(o.toString()) != null;
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
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/"+request.getId()+"->"+ CompositeQueryTestUtils.Q2().getName())
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        final QueryResourceInfo queryResourceInfo = GraphClient.unwrap(o.toString(), QueryResourceInfo.class);
                        assertTrue(queryResourceInfo.getAsgUrl().endsWith("fuse/query/1->q2/asg"));
                        assertTrue(queryResourceInfo.getCursorStoreUrl().endsWith("fuse/query/1->q2/cursor"));
                        return GraphClient.unwrap(o.toString()) != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/"+request.getId()+"->"+ CompositeQueryTestUtils.Q3().getName())
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        final QueryResourceInfo queryResourceInfo = GraphClient.unwrap(o.toString(), QueryResourceInfo.class);
                        assertTrue(queryResourceInfo.getAsgUrl().endsWith("fuse/query/1->q3/asg"));
                        assertTrue(queryResourceInfo.getCursorStoreUrl().endsWith("fuse/query/1->q3/cursor"));
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
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .delete("/fuse/query/1")
                .then()
                .assertThat()
                .statusCode(202)
                .contentType("application/json;charset=UTF-8");
        //validate deleted
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/1")
                .then()
                .assertThat()
                .statusCode(404)
                .contentType("application/json;charset=UTF-8");
        //validate inner deleted
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/1->q2")
                .then()
                .assertThat()
                .statusCode(404)
                .contentType("application/json;charset=UTF-8");
        //validate inner deleted
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/1->q3")
                .then()
                .assertThat()
                .statusCode(404)
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
        request.setQuery(CompositeQueryTestUtils.Q1());
        request.setCreateCursorRequest(createCursorRequest);
        //submit query
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .body(request)
                .post("/fuse/query")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/fuse/query/call[1]"));
                        assertTrue(data.get("cursorStoreUrl").toString().contains("/fuse/query/call[1]/cursor"));
                        assertTrue(data.get("v1QueryUrl").toString().endsWith("/fuse/query/call[1]/v1"));
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
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .body(request)
                .post("/fuse/query")
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
                        assertTrue(errorContent.get("errorDescription").toString().contains("Ontology Contains two adjacent Entities without relation inside"));
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
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .body(request)
                .post("/fuse/query")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/fuse/query/1"));
                        assertTrue(data.get("cursorStoreUrl").toString().endsWith("/fuse/query/1/cursor"));
                        assertTrue(data.get("v1QueryUrl").toString().endsWith("/fuse/query/1/v1"));
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
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/1")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/fuse/query/1"));
                        assertTrue(data.get("cursorStoreUrl").toString().endsWith("/fuse/query/1/cursor"));
                        assertTrue(data.get("v1QueryUrl").toString().endsWith("/fuse/query/1/v1"));
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
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/1/cursor")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/fuse/query/1/cursor"));
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
        BaseGraphClient GraphClient = new BaseGraphClient("http://localhost:8888/fuse");

        //query request
        Query query = TestUtils.loadQuery("Q001.json");
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(query);
        //submit query
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .body(request)
                .post("/fuse/query")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/fuse/query/1"));
                        assertTrue(data.get("cursorStoreUrl").toString().endsWith("/fuse/query/1/cursor"));
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
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/1")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/fuse/query/1"));
                        assertTrue(data.get("cursorStoreUrl").toString().endsWith("/fuse/query/1/cursor"));
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
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/1/v1/print")
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
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .delete("/fuse/query/1")
                .then()
                .assertThat()
                .statusCode(202)
                .contentType("application/json;charset=UTF-8");
    }

    @Test
    public void queryCreateWithInnerQueryAndDeleteResource() throws IOException {
        BaseGraphClient GraphClient = new BaseGraphClient("http://localhost:8888/fuse");

        //query request
        Query query = CompositeQueryTestUtils.Q1();
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(query);
        //submit query
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .body(request)
                .post("/fuse/query")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        final QueryResourceInfo queryResourceInfo = GraphClient.unwrap(o.toString(), QueryResourceInfo.class);
                        assertTrue(queryResourceInfo.getAsgUrl().endsWith("fuse/query/1/asg"));
                        assertTrue(queryResourceInfo.getCursorStoreUrl().endsWith("fuse/query/1/cursor"));
                        assertEquals(1,queryResourceInfo.getInnerUrlResourceInfos().size());
                        assertTrue(queryResourceInfo.getInnerUrlResourceInfos().get(0).getAsgUrl().endsWith("fuse/query/1->q2/asg"));
                        assertTrue(queryResourceInfo.getInnerUrlResourceInfos().get(0).getCursorStoreUrl().endsWith("fuse/query/1->q2/cursor"));
                        return GraphClient.unwrap(o.toString()) != null;
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
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/1")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/fuse/query/1"));
                        assertTrue(data.get("cursorStoreUrl").toString().endsWith("/fuse/query/1/cursor"));
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/1->q2")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/fuse/query/1->q2"));
                        assertTrue(data.get("cursorStoreUrl").toString().endsWith("/fuse/query/1->q2/cursor"));
                        return contentResponse.getData() != null;
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
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .delete("/fuse/query/1")
                .then()
                .assertThat()
                .statusCode(202)
                .contentType("application/json;charset=UTF-8");
        //validate deleted
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/1")
                .then()
                .assertThat()
                .statusCode(404)
                .contentType("application/json;charset=UTF-8");
        //validate inner deleted
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/1->q2")
                .then()
                .assertThat()
                .statusCode(404)
                .contentType("application/json;charset=UTF-8");
    }


}
