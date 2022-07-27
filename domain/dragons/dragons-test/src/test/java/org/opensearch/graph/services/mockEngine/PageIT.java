package org.opensearch.graph.services.mockEngine;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.model.transport.CreatePageRequest;
import org.opensearch.graph.model.transport.CreateQueryRequest;
import org.opensearch.graph.model.transport.cursor.CreateCursorRequest;
import org.opensearch.graph.model.transport.cursor.CreateGraphCursorRequest;
import org.opensearch.graph.services.TestsConfiguration;
import org.opensearch.graph.test.BaseITMarker;
import io.restassured.http.Header;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertTrue;

public class PageIT implements BaseITMarker {
    @Before
    public void before() {
        Assume.assumeTrue(TestsConfiguration.instance.shouldRunTestClass(this.getClass()));
    }

    @Test
    /**
     * execute query with expected plan result
     */
    public void page() throws IOException {
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
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");


        //create cursor resource
        AtomicReference<String> cursorId = new AtomicReference<>();
        CreateCursorRequest cursorRequest = new CreateGraphCursorRequest();
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .body(cursorRequest)
                .post("/opengraph/query/1/cursor")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        cursorId.set(data.get("resourceId").toString());
                        assertTrue(data.containsKey("cursorRequest"));
                        assertTrue(data.containsKey("pageStoreUrl"));
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");

        //get cursor resource by id
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .get("/opengraph/query/1/cursor/"+cursorId.get())
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.containsKey("cursorRequest"));
                        assertTrue(data.containsKey("pageStoreUrl"));
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");


        //create cursor page resource
        CreatePageRequest pageRequest = new CreatePageRequest();
        pageRequest.setPageSize(100);
        AtomicReference<String> pageId = new AtomicReference<>();
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .body(pageRequest)
                .post("/opengraph/query/1/cursor/"+cursorId.get()+"/page")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        pageId.set(data.get("resourceId").toString());
                        assertTrue(data.containsKey("dataUrl"));
                        assertTrue(data.containsKey("actualPageSize"));
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");

        //get cursor page resource by id
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .get("/opengraph/query/1/cursor/"+cursorId.get() +"/page/"+pageId.get())
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.containsKey("dataUrl"));
                        assertTrue(data.containsKey("actualPageSize"));
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
    }

    @Test
    /**
     * execute query with expected plan result
     */
    public void pageAndFetch() throws IOException {
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
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");


        //create cursor resource
        AtomicReference<String> cursorId = new AtomicReference<>();
        CreateCursorRequest cursorRequest = new CreateGraphCursorRequest();
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .body(cursorRequest)
                .post("/opengraph/query/1/cursor")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        cursorId.set(data.get("resourceId").toString());
                        assertTrue(data.containsKey("cursorRequest"));
                        assertTrue(data.containsKey("pageStoreUrl"));
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");

        //get cursor resource by id
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .get("/opengraph/query/1/cursor/"+cursorId.get())
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.containsKey("cursorRequest"));
                        assertTrue(data.containsKey("pageStoreUrl"));
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");


        //create cursor page resource
        CreatePageRequest pageRequest = new CreatePageRequest(100, true);
        AtomicReference<String> pageId = new AtomicReference<>();
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .body(pageRequest)
                .post("/opengraph/query/1/cursor/"+cursorId.get()+"/page")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        pageId.set(data.get("resourceId").toString());
                        assertTrue(data.containsKey("dataUrl"));
                        assertTrue(data.containsKey("actualPageSize"));
                        assertTrue(data.containsKey("data"));
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");

        //get cursor page resource by id
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .get("/opengraph/query/1/cursor/"+cursorId.get() +"/page/"+pageId.get())
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.containsKey("dataUrl"));
                        assertTrue(data.containsKey("actualPageSize"));
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
    }

    @Test
    public void pages() throws IOException {
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
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");


        //create cuyrsor resource
        AtomicReference<String> cursorId = new AtomicReference<>();
        CreateCursorRequest cursorRequest = new CreateGraphCursorRequest();
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .body(cursorRequest)
                .post("/opengraph/query/1/cursor")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        cursorId.set(data.get("resourceId").toString());
                        assertTrue(data.containsKey("cursorRequest"));
                        assertTrue(data.containsKey("pageStoreUrl"));
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");

        //create cursor page resource
        CreatePageRequest pageRequest = new CreatePageRequest();
        pageRequest.setPageSize(100);
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .body(pageRequest)
                .post("/opengraph/query/1/cursor/"+cursorId.get()+"/page")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.containsKey("dataUrl"));
                        assertTrue(data.containsKey("actualPageSize"));
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");


        //get cursor pages resource
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .get("/opengraph/query/1/cursor/"+cursorId.get())
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.containsKey("cursorRequest"));
                        assertTrue(data.containsKey("pageStoreUrl"));
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");


        //get cursor page resource by id
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .get("/opengraph/query/1/cursor/"+cursorId.get()+"/page")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.containsKey("resourceUrls"));
                        assertTrue(((List) data.get("resourceUrls")).size() > 0);
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");


    }

}
