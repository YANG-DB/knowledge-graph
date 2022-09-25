package org.opensearch.graph.services.mockEngine;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.graph.client.BaseGraphClient;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.model.transport.CreateQueryRequest;
import org.opensearch.graph.services.TestsConfiguration;
import org.opensearch.graph.test.BaseITMarker;
import io.restassured.http.Header;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertTrue;

public class PlanIT implements BaseITMarker {
    @Before
    public void before() {
        Assume.assumeTrue(TestsConfiguration.instance.shouldRunTestClass(this.getClass()));
    }

    @Test
    /**
     * execute query with expected plan result
     */
    public void plan() throws IOException {
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
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");


        //get plan resource by id
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .get("/opengraph/query/1/plan")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        return o.toString().contains("ops");
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");

        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .get("/opengraph/query/1/plan/print")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        String data = GraphClient.unwrap(o.toString());
                        return data!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");

    }

}
