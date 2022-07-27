package org.opensearch.graph.client;

/*-
 * #%L
 * opengraph-core
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */







import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.opensearch.graph.dispatcher.cursor.CompositeCursorFactory;
import org.opensearch.graph.dispatcher.cursor.CreateCursorRequestDeserializer;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.logical.LogicalGraphModel;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.resourceInfo.*;
import org.opensearch.graph.model.results.AssignmentsQueryResult;
import org.opensearch.graph.model.results.Entity;
import org.opensearch.graph.model.results.QueryResultBase;
import org.opensearch.graph.model.results.Relationship;
import org.opensearch.graph.model.transport.*;
import org.opensearch.graph.model.transport.cursor.CreateCursorRequest;
import org.opensearch.graph.model.transport.cursor.CreatePathsCursorRequest;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.jooby.MediaType;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

import static org.opensearch.graph.client.GraphClient.*;
import static org.opensearch.graph.model.transport.CreateQueryRequestMetadata.*;
import static io.restassured.RestAssured.given;

public class BaseGraphClient implements GraphClient {
    //region Constructor
    public BaseGraphClient(String graphUrl) throws IOException {
        this.graphUrl = graphUrl;
        this.objectMapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addDeserializer(CreateCursorRequest.class,
                new CreateCursorRequestDeserializer(Stream.ofAll(this.getCursorBindings().entrySet())
                        .map(entry -> new CompositeCursorFactory.Binding(entry.getKey(), entry.getValue(), null))
                        .toJavaList()));
        this.objectMapper.registerModule(module);
    }
    //endregion

    //region Public Methods
    @Override
    public GraphResourceInfo getInfo() throws IOException {
        return this.objectMapper.readValue(unwrap(getRequest(this.graphUrl)), GraphResourceInfo.class);
    }

    @Override
    public Object getId(String name, int numIds) throws IOException {
        return this.objectMapper.readValue(unwrap(getRequest(this.graphUrl + "/idgen/" + name + "?numIds=" + numIds)), Map.class);
    }

    private <T> ResultResourceInfo loadGraphData(String ontology, String resourceURl, T root) throws IOException {
        // URL:/fuse/load/ontology/:id/load
        return new ResultResourceInfo<>(resourceURl, ontology, unwrap(postRequest(resourceURl, root)));
    }


    @Override
    public ResultResourceInfo upsertGraphData(String ontology, URL resource) throws IOException {
        return loadGraphData(ontology,String.format("%s/load/ontology/%s/graph/load?directive=%s", this.graphUrl, ontology,"UPSERT"),
                objectMapper.readValue(resource,LogicalGraphModel.class));
    }

    @Override
    public ResultResourceInfo loadGraphData(String ontology, LogicalGraphModel model) throws IOException {
        String resourceUrl = String.format("%s/load/ontology/%s/graph/load?directive=%s", this.graphUrl, ontology, "INSERT");
        return new ResultResourceInfo<>(resourceUrl, ontology, unwrap(postRequest(resourceUrl,model)));
    }

    @Override
    public ResultResourceInfo loadGraphData(String ontology, URL resource) throws IOException {
        return loadGraphData(ontology,String.format("%s/load/ontology/%s/graph/load?directive=%s", this.graphUrl, ontology,"INSERT"),
                objectMapper.readValue(resource,LogicalGraphModel.class));
    }

    @Override
    public ResultResourceInfo uploadGraphFile(String ontology, URL resourceFile) throws URISyntaxException {
        return uploadFile(ontology, resourceFile, String.format("%s/load/ontology/%s/graph/upload?directive=%s", this.graphUrl, ontology,"INSERT"));
    }

    @Override
    public ResultResourceInfo upsertGraphFile(String ontology, URL resourceFile) throws IOException, URISyntaxException {
        return uploadFile(ontology, resourceFile, String.format("%s/load/ontology/%s/graph/upload?directive=%s", this.graphUrl, ontology,"UPSERT"));
    }


    @Override
    public ResultResourceInfo upsertCsvData(String ontology, String type, String label, URL resource) throws IOException, URISyntaxException {
        return uploadFile(ontology, resource, String.format("%s/load/ontology/%s/csv/upload?directive=%s&type=%s&label=%s", this.graphUrl, ontology,"UPSERT",type,label));
    }

    @Override
    public ResultResourceInfo loadCsvData(String ontology, String type, String label, String model) throws IOException {
        String resourceUrl = String.format("%s/load/ontology/%s/csv/load?directive=%s&type=%s&label=%s", this.graphUrl, ontology, "INSERT",type,label);
        return new ResultResourceInfo<>(resourceUrl, ontology, unwrap(postRequest(resourceUrl,model)));
    }

    @Override
    public ResultResourceInfo loadCsvData(String ontology, String type, String label, URL resource) throws IOException {
        return loadGraphData(ontology,String.format("%s/load/ontology/%s/csv/load?directive=%s&type=%s&label=%s", this.graphUrl, ontology,"INSERT",type,label),
                objectMapper.readValue(resource,String.class));
    }

    @Override
    public ResultResourceInfo uploadCsvFile(String ontology, String type, String label, URL resource) throws IOException, URISyntaxException {
        return uploadFile(ontology, resource, String.format("%s/load/ontology/%s/csv/upload?directive=%s&type=%s&label=%s", this.graphUrl, ontology,"UPSERT",type,label));
    }

    @Override
    public ResultResourceInfo upsertCsvFile(String ontology, String type, String label, URL resource) throws IOException, URISyntaxException {
        return uploadFile(ontology, resource, String.format("%s/load/ontology/%s/csv/upload?directive=%s&type=%s&label=%s", this.graphUrl, ontology,"INSERT",type,label));
    }

    private ResultResourceInfo uploadFile(String ontology, URL resourceFile, String resourceURl) throws URISyntaxException {
        final File file = new File(resourceFile.getFile());
        final URI uri = resourceFile.toURI();


        String result = given()
                .multiPart("file", file)
                .formParam("file", uri.toASCIIString())
                .contentType("multipart/form-data")
                .post(resourceURl)
                .thenReturn()
                .asString();

        return new ResultResourceInfo<>(resourceURl, ontology, result);
    }

    @Override
    public QueryResourceInfo postQuery(String queryStoreUrl, Query query) throws IOException {
        return postQuery(queryStoreUrl,query, PlanTraceOptions.of(PlanTraceOptions.Level.none));
    }

    @Override
    public QueryResourceInfo postQuery(String queryStoreUrl, String query, String ontology) throws IOException {
        return postCypherQuery(queryStoreUrl,query,ontology, PlanTraceOptions.of(PlanTraceOptions.Level.none));
    }

    @Override
    public QueryResourceInfo postQuery(String queryStoreUrl, CreateQueryRequest request) throws IOException {
        return this.objectMapper.readValue(unwrap(postRequest(queryStoreUrl +"/" + CreateQueryRequest.TYPE, request)), QueryResourceInfo.class);
    }

    @Override
    public QueryResourceInfo postQuery(String queryStoreUrl, Query query, PlanTraceOptions planTraceOptions) throws IOException {
        CreateQueryRequest request = new CreateQueryRequest();
        String id = UUID.randomUUID().toString();
        request.setId(id);
        request.setName(id);
        request.setQuery(query);
        request.setPlanTraceOptions(planTraceOptions);
        return this.objectMapper.readValue(unwrap(postRequest(queryStoreUrl +"/" + CreateQueryRequest.TYPE, request)), QueryResourceInfo.class);
    }

    @Override
    public QueryResourceInfo postGraphQLQuery(String queryStoreUrl, String query, String ontology, PlanTraceOptions planTraceOptions) throws IOException {
            CreateJsonQueryRequest request = new CreateJsonQueryRequest(TYPE_GRAPHQL);
            String id = UUID.randomUUID().toString();
            request.setId(id);
            request.setName(id);
            request.setQuery(query);
            request.setOntology(ontology);
            request.setPlanTraceOptions(planTraceOptions);
            final String response = postRequest(queryStoreUrl +"/" + TYPE_GRAPHQL, request);
            return this.objectMapper.readValue(unwrap(response), QueryResourceInfo.class);
    }

    @Override
    public QueryResourceInfo postCypherQuery(String queryStoreUrl, String query, String ontology, PlanTraceOptions planTraceOptions) throws IOException {
        CreateJsonQueryRequest request = new CreateJsonQueryRequest(TYPE_CYPHERQL);
        String id = UUID.randomUUID().toString();
        request.setId(id);
        request.setName(id);
        request.setQuery(query);
        request.setOntology(ontology);
        request.setPlanTraceOptions(planTraceOptions);
        final String response = postRequest(queryStoreUrl +"/" + CreateJsonQueryRequest.TYPE_CYPHERQL, request);
        return this.objectMapper.readValue(unwrap(response), QueryResourceInfo.class);
    }

    @Override
    public QueryResourceInfo postQuery(String queryStoreUrl, Query query, String id, String name) throws IOException {
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId(id);
        request.setName(name);
        request.setQuery(query);
        return this.objectMapper.readValue(unwrap(postRequest(queryStoreUrl +"/" + CreateQueryRequest.TYPE, request)), QueryResourceInfo.class);
    }

    @Override
    public QueryResourceInfo postQuery(String queryStoreUrl, Query query, String id, String name, CreateCursorRequest createCursorRequest) throws IOException {
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId(id);
        request.setName(name);
        request.setQuery(query);
        request.setCreateCursorRequest(createCursorRequest);
        return this.objectMapper.readValue(unwrap(postRequest(queryStoreUrl +"/" + CreateQueryRequest.TYPE, request)), QueryResourceInfo.class);
    }

    @Override
    public String initIndices(String ontology) {
        return getRequest(String.format("%s/load/ontology/%s/init",this.graphUrl, ontology));
    }

    @Override
    public String dropIndices(String ontology) {
        return getRequest(String.format("%s/load/ontology/%s/drop",this.graphUrl, ontology));
    }

    private Map<String, Class<? extends CreateCursorRequest>> getCursorBindings() throws IOException {
        Map<String, String> cursorBindingStrings = unwrap(getRequest(this.graphUrl + "/internal/cursorBindings"), Map.class);

        return Stream.ofAll(cursorBindingStrings.entrySet())
                .toJavaMap(entry -> {
                    try {
                        return new Tuple2<>(entry.getKey(), (Class<? extends CreateCursorRequest>)Class.forName(entry.getValue()));
                    } catch (ClassNotFoundException e) {
                        return new Tuple2<>(entry.getKey(), null);
                    }
                });
    }

    @Override
    public CursorResourceInfo postCursor(String cursorStoreUrl) throws IOException {
        return this.postCursor(cursorStoreUrl, new CreatePathsCursorRequest());
    }

    @Override
    public CursorResourceInfo postCursor(String cursorStoreUrl, CreateCursorRequest cursorRequest) throws IOException {
        return this.objectMapper.readValue(unwrap(postRequest(cursorStoreUrl, cursorRequest)), CursorResourceInfo.class);
    }

    @Override
    public PageResourceInfo postPage(String pageStoreUrl, int pageSize) throws IOException {
        CreatePageRequest request = new CreatePageRequest();
        request.setPageSize(pageSize);

        return this.objectMapper.readValue(unwrap(postRequest(pageStoreUrl, request)), PageResourceInfo.class);
    }

    @Override
    public PageResourceInfo getPage(String pageUrl, String pageId) throws IOException {
        return this.objectMapper.readValue(unwrap(getRequest(pageUrl+"/"+pageId)), PageResourceInfo.class);
    }

    @Override
    public PageResourceInfo getPage(String pageUrl) throws IOException {
        return this.objectMapper.readValue(unwrap(getRequest(pageUrl)), PageResourceInfo.class);
    }

    @Override
    public QueryResourceInfo getQuery(String queryUrl, String queryId) throws IOException {
        return this.objectMapper.readValue(unwrap(getRequest(queryUrl+"/"+queryId)), QueryResourceInfo.class);
    }

    @Override
    public CursorResourceInfo getCursor(String cursorUrl, String cursorId) throws IOException {
        return this.objectMapper.readValue(unwrap(getRequest(cursorUrl+"/"+cursorId)), CursorResourceInfo.class);
    }

    @Override
    public Ontology getOntology(String ontologyUrl) throws IOException {
        return this.objectMapper.readValue(unwrap(getRequest(ontologyUrl)), Ontology.class);
    }

    @Override
    public Query getQuery(String queryUrl,Class<? extends Query> klass) throws IOException {
        return unwrap(getRequest(queryUrl), klass);
    }

    @Override
    public QueryResultBase getPageData(String pageDataUrl) throws IOException {
        return this.objectMapper.readValue(unwrap(getRequest(pageDataUrl)), new TypeReference<AssignmentsQueryResult<Entity,Relationship>>() {});
    }

    @Override
    public QueryResultBase getPageData(String pageDataUrl, TypeReference<? extends QueryResultBase> typeReference) throws IOException {
        return this.objectMapper.readValue(unwrap(getRequest(pageDataUrl)), typeReference);
    }

    @Override
    public String getPageDataPlain(String pageDataUrl) throws IOException {
        return getRequest(pageDataUrl, MediaType.plain.name());
    }

    @Override
    public String getPlan(String planUrl) throws IOException {
        return getRequest(planUrl);
    }

    @Override
    public Plan getPlanObject(String planUrl) throws IOException {
        PlanWithCost<Plan, PlanDetailedCost> planWithCost = unwrapDouble(getRequest(planUrl));
        return planWithCost.getPlan();

    }

    @Override
    public Long getSnowflakeId() throws IOException {
        return this.objectMapper.readValue(unwrap(getRequest(this.graphUrl +"/internal/snowflakeId")), Long.class);
    }

    @Override
    public String getGraphUrl() {
        return graphUrl;
    }

    //endregion

    @Override
    public String deleteQuery(QueryResourceInfo queryResourceInfo) {
        return given().contentType("application/json")
                .delete(queryResourceInfo.getResourceUrl())
                .thenReturn()
                .asString();
    }

    @Override
    public boolean shutdown() {
        return true;
    }

    public String unwrap(String response) throws IOException {
        Map<String, Object> responseMap = this.objectMapper.readValue(response, new TypeReference<Map<String, Object>>(){});
        return this.objectMapper.writeValueAsString(responseMap.get("data"));
    }

    public <T> T unwrap(String response, Class<T> klass) throws IOException {
        return this.objectMapper.readValue(unwrap(response), klass);
    }

    //endregion

    //region Fields
    private String graphUrl;

    private ObjectMapper objectMapper;
    //endregion
}
