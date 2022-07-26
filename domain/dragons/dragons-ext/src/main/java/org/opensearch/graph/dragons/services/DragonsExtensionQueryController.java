package org.opensearch.graph.dragons.services;



import com.google.inject.Inject;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.planTree.PlanNode;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.resourceInfo.QueryResourceInfo;
import org.opensearch.graph.model.resourceInfo.StoreResourceInfo;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.model.transport.CreateJsonQueryRequest;
import org.opensearch.graph.model.transport.CreateQueryRequest;
import org.opensearch.graph.model.transport.ExecuteStoredQueryRequest;
import org.opensearch.graph.model.validation.ValidationResult;
import org.opensearch.graph.services.controllers.QueryController;
import org.opensearch.graph.dragons.driver.ExtensionQueryDriver;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Optional;

public class DragonsExtensionQueryController implements QueryController<QueryController, ExtensionQueryDriver> {

    private final ExtensionQueryDriver driver;
    private final QueryController controller;

    //region Constructors
    @Inject
    public DragonsExtensionQueryController(
            ExtensionQueryDriver driver,
            QueryController controller) {

        this.driver = driver;
        this.controller = (QueryController) controller.driver(driver);
    }

    @Override
    public ContentResponse<QueryResourceInfo> create(CreateQueryRequest request) {
        return controller.create(request);
    }

    @Override
    public ContentResponse<QueryResourceInfo> create(CreateJsonQueryRequest request) {
        return controller.create(request);
    }

    @Override
    public ContentResponse<Object> runV1Query(Query query, int pageSize, String cursorType) {
        return controller.runV1Query(query,pageSize,cursorType);
    }

    @Override
    public ContentResponse<Object> runCypher(String cypher, String ontology, int pageSize, String cursorType) {
        return controller.runCypher(cypher, ontology, pageSize, cursorType);
    }

    @Override
    public ContentResponse<Object> runGraphQL(String graphQL, String ontology, int pageSize, String cursorType) {
        return controller.runGraphQL(graphQL, ontology, pageSize, cursorType);
    }

    @Override
    public ContentResponse<Object> runSparql(String sparql, String ontology, int pageSize, String cursorType) {
        return controller.runSparql(sparql, ontology, pageSize, cursorType);
    }

    @Override
    public ContentResponse<ValidationResult> validate(Query query) {
        return controller.validate(query);
    }

    @Override
    public ContentResponse<Object> getVertex(String ontology, String type, String id) {
        return controller.getVertex(ontology,ontology,id);
    }

    @Override
    public ContentResponse<Object> getNeighbors(String ontology, String type, String id) {
        return controller.getNeighbors(ontology,type,id);
    }


    @Override
    public ContentResponse<Object> findPath(String ontology, String sourceEntity, String sourceId, String targetEntity, String targetId, String relationType, int maxHops) {
        return controller.findPath(ontology,sourceEntity,sourceId,targetEntity,targetId,relationType,maxHops);
    }

    @Override
    public ContentResponse<Object> runCypher(String cypher, String ontology) {
        return controller.runCypher(cypher,ontology);
    }

    @Override
    public ContentResponse<QueryResourceInfo> createAndFetch(CreateQueryRequest request) {
        return controller.createAndFetch(request);
    }

    @Override
    public ContentResponse<QueryResourceInfo> createAndFetch(CreateJsonQueryRequest request) {
        return controller.createAndFetch(request);
    }

    @Override
    public ContentResponse<QueryResourceInfo> callAndFetch(ExecuteStoredQueryRequest request) {
       return controller.callAndFetch(request);
    }

    @Override
    public ContentResponse<Object> fetchNextPage(String queryId, Optional<String> cursorId, int pageSize, boolean deleteCurrentPage) {
        return controller.fetchNextPage(queryId,cursorId,pageSize,deleteCurrentPage);
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo() {
        return controller.getInfo();
    }

    @Override
    public ContentResponse<QueryResourceInfo> getInfo(String queryId) {
        return controller.getInfo(queryId);
    }

    @Override
    public ContentResponse<Query> getV1(String queryId) {
        return controller.getV1(queryId);
    }

    @Override
    public ContentResponse<AsgQuery> getAsg(String queryId) {
        return controller.getAsg(queryId);
    }

    @Override
    public ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> explain(String queryId) {
        return controller.explain(queryId);
    }

    @Override
    public ContentResponse<Object> profile(String queryId) {
        return controller.profile(queryId);
    }

    @Override
    public ContentResponse<PlanNode<Plan>> planVerbose(String queryId) {
        return controller.planVerbose(queryId);
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId) {
        return controller.delete(queryId);
    }

    @Override
    public ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> plan(Query query) {
        return controller.plan(query);
    }

    @Override
    public ContentResponse<GraphTraversal> traversal(Query query) {
        return controller.traversal(query);
    }

    @Override
    public QueryController driver(ExtensionQueryDriver driver) {
        return this;
    }

    //endregion

}
