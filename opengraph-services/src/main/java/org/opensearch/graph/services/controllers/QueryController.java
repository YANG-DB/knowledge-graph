package org.opensearch.graph.services.controllers;


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
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Optional;

/**
 * Created by lior.perry on 19/02/2017.
 */
public interface QueryController<C,D> extends Controller<C,D> {
    /**
     * create a prepared statement
     * type may be volatile or persistent
     * @param request
     * @return
     */
    ContentResponse<QueryResourceInfo> create(CreateQueryRequest request);
    /**
     * create a prepared statement
     * type may be volatile or persistent
     * @param request
     * @return
     */
    ContentResponse<QueryResourceInfo> create(CreateJsonQueryRequest request);

    /**
     * run a stateless query and get immediate graph results (first page only)
     * type may be volatile or persistent
     * @param `query
     * @param pageSize
     * @param cursorType
     * @return
     */
    ContentResponse<Object> runV1Query(Query query, int pageSize, String cursorType);

    /**
     * run a stateless query and get immediate graph results (first page only)
     * type may be volatile or persistent
     * @param query
     * @return
     */
    ContentResponse<ValidationResult> validate(Query query);

    /**
     * run findPath graph query
     *
     * */
    ContentResponse<Object> findPath(String ontology, String sourceEntity, String sourceId, String targetEntity,String targetId, String relationType, int maxHops);


    /**
     * get vertex by id
     *
     * */
    ContentResponse<Object> getVertex(String ontology, String type, String id);

    /**
     * get vertex and its neighbors by id
     *
     * */
    ContentResponse<Object> getNeighbors(String ontology, String type, String id);

    /**
     * run a stateless cypher query and get immediate graph results (first page only)
     * type may be volatile or persistent
     * @param cypher
     * @param ontology
     * @return
     */
    ContentResponse<Object> runCypher(String cypher, String ontology);


    /**
     * run a stateless cypher query and get immediate graph results (first page only)
     * type may be volatile or persistent
     * @param `query
     * @param ontology
     * @param pageSize
     * @param cursorType
     * @return
     */
    ContentResponse<Object> runCypher(String cypher, String ontology, int pageSize, String cursorType);

    /**
     * run a stateless graphQL query and get immediate graph results (first page only)
     * type may be volatile or persistent
     * @param `query
     * @param ontology
     * @param pageSize
     * @param cursorType
     * @return
     */
    ContentResponse<Object> runGraphQL(String graphQL, String ontology, int pageSize, String cursorType);

    /**
     * run a stateless sparql query and get immediate graph results (first page only)
     * type may be volatile or persistent
     * @param `query
     * @param ontology
     * @param pageSize
     * @param cursorType
     * @return
     */
    ContentResponse<Object> runSparql(String sparql, String ontology, int pageSize, String cursorType);


    /**
     * create a prepared statement, run against db and return results
     * type may be volatile or persistent
     * @param request
     * @return
     */
    ContentResponse<QueryResourceInfo> createAndFetch(CreateQueryRequest request);

    /**
     * create a prepared statement, run against db and return results
     * type may be volatile or persistent
     * @param request
     * @return
     */
    ContentResponse<QueryResourceInfo> createAndFetch(CreateJsonQueryRequest request);

    /**
     * call existing statement and, run against db and return results
     * @param request
     * @return
     */
    ContentResponse<QueryResourceInfo> callAndFetch(ExecuteStoredQueryRequest request);


    /**
     *
     * @param queryId
     * @param cursorId
     * @param pageSize
     * @param deleteCurrentPage
     * @return
     */
    ContentResponse<Object> fetchNextPage(String queryId, Optional<String> cursorId, int pageSize, boolean deleteCurrentPage);

    /**
     * get queries info
     * @return
     */
    ContentResponse<StoreResourceInfo> getInfo();

    /**
     * get specific query info
     * @param queryId
     * @return
     */
    ContentResponse<QueryResourceInfo> getInfo(String queryId);

    /**
     * get v1 query for a given query
     * @param queryId
     * @return
     */
    ContentResponse<Query> getV1(String queryId);
    /**
     * get asg query for a given query
     * @param queryId
     * @return
     */
    ContentResponse<AsgQuery> getAsg(String queryId);
    /**
     * explain execution plan for a given query
     * @param queryId
     * @return
     */
    ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> explain(String queryId);
    /**
     * profile query by execution and returning profile info
     * @param queryId
     * @return
     */
    ContentResponse<Object> profile(String queryId);

    ContentResponse<PlanNode<Plan>> planVerbose(String queryId);

    /**
     * delete query resource
     * @param queryId
     * @return
     */
    ContentResponse<Boolean> delete(String queryId);

    /**
     * get the plan generated by the v1 query
     * @param query
     * @return
     */
    ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> plan(Query query);

    /**
     * get the traversal execution instruction - gremlin
     * @param query
     * @return
     */
    ContentResponse<GraphTraversal> traversal(Query query);
}
