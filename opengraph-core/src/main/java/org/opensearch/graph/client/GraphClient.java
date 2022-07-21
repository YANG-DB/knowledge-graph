package org.opensearch.graph.client;




import com.cedarsoftware.util.io.JsonReader;
import com.fasterxml.jackson.core.type.TypeReference;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.logical.LogicalGraphModel;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.resourceInfo.*;
import org.opensearch.graph.model.results.QueryResultBase;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.model.transport.CreateQueryRequest;
import org.opensearch.graph.model.transport.PlanTraceOptions;
import org.opensearch.graph.model.transport.cursor.CreateCursorRequest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static io.restassured.RestAssured.given;

public interface GraphClient {
    //region Protected Methods

    static String postRequest(String url, Object body) throws IOException {
      return given().contentType("application/json")
                .body(body)
                .post(url)
                .thenReturn()
                .asString();
    }

    static String getRequest(String url) {
        return getRequest(url, "application/json");
    }

    static String getRequest(String url, String contentType) {
        return given().contentType(contentType)
                .get(url)
                .thenReturn()
                .asString();
    }

    static <T> T unwrapDouble(String response) throws IOException {
        return ((ContentResponse<T>) JsonReader.jsonToJava((String) JsonReader.jsonToJava(response))).getData();
    }

    //region Public Methods
    FuseResourceInfo getFuseInfo() throws IOException;

    Object getId(String name, int numIds) throws IOException;

    ResultResourceInfo upsertGraphData(String ontology, URL resource) throws IOException;

    /**
     * load graph data file (logical graph model) according to technical id
     * @param ontology
     * @param model
     * @return
     * @throws IOException
     */
    ResultResourceInfo loadGraphData(String ontology, LogicalGraphModel model) throws IOException;

    /**
     * load data file (logical graph model) according to technical id
     * @param ontology
     * @param resource
     * @return
     * @throws IOException
     */
    ResultResourceInfo loadGraphData(String ontology, URL resource) throws IOException;

    /**
     *
     * upload data file (logical graph model) according to technical id
     *
     * @param ontology
     * @param resource
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    ResultResourceInfo uploadGraphFile(String ontology, URL resource) throws IOException, URISyntaxException;

    /**
     *
     * upsert data file (logical graph model) according to technical id
     *
     * @param ontology
     * @param resource
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    ResultResourceInfo upsertGraphFile(String ontology, URL resource) throws IOException, URISyntaxException;

    /**
     * upsert graph data file (logical graph model) according to technical id
     * @param ontology
     * @param resource
     * @return
     * @throws IOException
     */
    ResultResourceInfo upsertCsvData(String ontology, String type, String label, URL resource) throws IOException, URISyntaxException;

    /**
     * load graph data file (logical graph model) according to technical id
     * @param ontology
     * @param model
     * @return
     * @throws IOException
     */
    ResultResourceInfo loadCsvData(String ontology, String type, String label, String model) throws IOException;

    /**
     * load data file (logical graph model) according to technical id
     * @param ontology
     * @param resource
     * @return
     * @throws IOException
     */
    ResultResourceInfo loadCsvData(String ontology, String type, String label, URL resource) throws IOException;

    /**
     *
     * upload data file (logical graph model) according to technical id
     *
     * @param ontology
     * @param type
     * @param label
     * @param resource
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    ResultResourceInfo uploadCsvFile(String ontology, String type, String label, URL resource) throws IOException, URISyntaxException;

    /**
     *
     * upsert data file (logical graph model) according to technical id
     *
     * @param ontology
     * @param resource
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    ResultResourceInfo upsertCsvFile(String ontology, String type, String label, URL resource) throws IOException, URISyntaxException;



    QueryResourceInfo postQuery(String queryStoreUrl, Query query) throws IOException;

    QueryResourceInfo postQuery(String queryStoreUrl, String query, String ontology) throws IOException;

    QueryResourceInfo postQuery(String queryStoreUrl, CreateQueryRequest request) throws IOException;

    QueryResourceInfo postQuery(String queryStoreUrl, Query query, PlanTraceOptions planTraceOptions) throws IOException;

    QueryResourceInfo postCypherQuery(String queryStoreUrl, String query, String ontology, PlanTraceOptions planTraceOptions) throws IOException;

    QueryResourceInfo postGraphQLQuery(String queryStoreUrl, String query, String ontology, PlanTraceOptions planTraceOptions) throws IOException;

    QueryResourceInfo postQuery(String queryStoreUrl, Query query, String id, String name) throws IOException;

    QueryResourceInfo postQuery(String queryStoreUrl, Query query, String id, String name, CreateCursorRequest createCursorRequest) throws IOException;

    /**
     * call "fuse/load/ontology/{id}/init"
     * @param ontology
     * @return
     */
    String initIndices(String ontology);

    /**
     * call "fuse/load/ontology/{id}/drop"
     * @param ontology
     * @return
     */
    String dropIndices(String ontology);

    CursorResourceInfo postCursor(String cursorStoreUrl) throws IOException;

    CursorResourceInfo postCursor(String cursorStoreUrl, CreateCursorRequest cursorRequest) throws IOException;

    PageResourceInfo postPage(String pageStoreUrl, int pageSize) throws IOException;

    PageResourceInfo getPage(String pageUrl, String pageId) throws IOException;

    PageResourceInfo getPage(String pageUrl) throws IOException;

    QueryResourceInfo getQuery(String queryUrl, String queryId) throws IOException;

    CursorResourceInfo getCursor(String cursorUrl, String cursorId) throws IOException;

    Ontology getOntology(String ontologyUrl) throws IOException;

    Query getQuery(String queryUrl,Class<? extends Query> klass) throws IOException;

    QueryResultBase getPageData(String pageDataUrl, TypeReference<? extends QueryResultBase> typeReference) throws IOException ;

    QueryResultBase getPageData(String pageDataUrl) throws IOException;

    String getPageDataPlain(String pageDataUrl) throws IOException;

    String getPlan(String planUrl) throws IOException;

    Plan getPlanObject(String planUrl) throws IOException;

    Long getFuseSnowflakeId() throws IOException;

    String getFuseUrl();

    String deleteQuery(QueryResourceInfo queryResourceInfo);

    boolean shutdown();
}
