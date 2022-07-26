package org.opensearch.graph.services.controllers;




import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.opensearch.graph.dispatcher.driver.QueryDriver;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.planTree.PlanNode;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.resourceInfo.CursorResourceInfo;
import org.opensearch.graph.model.resourceInfo.PageResourceInfo;
import org.opensearch.graph.model.resourceInfo.QueryResourceInfo;
import org.opensearch.graph.model.resourceInfo.StoreResourceInfo;
import org.opensearch.graph.model.transport.*;
import org.opensearch.graph.model.transport.ContentResponse.Builder;
import org.opensearch.graph.model.validation.ValidationResult;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Collections;
import java.util.Optional;

import static org.opensearch.graph.model.transport.Status.*;

/**
 * Created by lior.perry on 19/02/2017.
 */
public class StandardQueryController implements QueryController<QueryController,QueryDriver> {
    public static final String cursorControllerParameter = "StandardQueryController.@cursorController";
    public static final String pageControllerParameter = "StandardQueryController.@pageController";

    //region Constructors
    @Inject
    public StandardQueryController(
            QueryDriver driver,
            @Named(cursorControllerParameter) CursorController cursorController,
            @Named(pageControllerParameter) PageController pageController) {
        this.driver = driver;
        this.cursorController = cursorController;
        this.pageController = pageController;
    }
    //endregion

    //region QueryController Implementation
    @Override
    public ContentResponse<QueryResourceInfo> create(CreateQueryRequest request) {
        return Builder.<QueryResourceInfo>builder(CREATED, INTERNAL_SERVER_ERROR )
                .data(driver().create(request))
                .successPredicate(response -> response.getData() != null && response.getData().getError() == null)
                .compose();
    }

    protected QueryDriver driver() {
        return driver;
    }

    @Override
    public ContentResponse<QueryResourceInfo> create(CreateJsonQueryRequest request) {
        return Builder.<QueryResourceInfo>builder(CREATED, INTERNAL_SERVER_ERROR )
                .data(driver().create(request))
                .successPredicate(response -> response.getData() != null && response.getData().getError() == null)
                .compose();
    }

    @Override
    public ContentResponse<Object> runV1Query(Query query, int pageSize, String cursorType) {
        return Builder.builder(CREATED, INTERNAL_SERVER_ERROR )
                .data(driver().run(query,pageSize,cursorType ))
                .successPredicate(objectContentResponse -> true)
                .compose();

    }

    @Override
    public ContentResponse<ValidationResult> validate(Query query) {
        return Builder.<ValidationResult>builder(CREATED, INTERNAL_SERVER_ERROR )
                .data(Optional.of(driver().validateAndRewriteQuery(query)))
                .successPredicate(objectContentResponse -> true)
                .compose();
    }

    @Override
    public ContentResponse<Object> findPath(String ontology, String sourceEntity, String sourceId, String targetEntity,String targetId, String relationType, int maxHops) {
        return Builder.builder(CREATED, INTERNAL_SERVER_ERROR )
                .data(Optional.of(driver().findPath(ontology,sourceEntity,sourceId,targetEntity,targetId,relationType,maxHops)))
                .successPredicate(objectContentResponse -> true)
                .compose();
    }

    @Override
    public ContentResponse<Object> getVertex(String ontology, String type, String id) {
        return Builder.builder(CREATED, INTERNAL_SERVER_ERROR )
                .data(Optional.of(driver().getVertex(ontology,type,id)))
                .successPredicate(objectContentResponse -> true)
                .compose();
    }

    @Override
    public ContentResponse<Object> getNeighbors(String ontology, String type, String id) {
        return Builder.builder(CREATED, INTERNAL_SERVER_ERROR )
                .data(Optional.of(driver().getNeighbors(ontology,type,id)))
                .successPredicate(objectContentResponse -> true)
                .compose();

    }

    @Override
    public ContentResponse<Object> runCypher(String cypher, String ontology) {
        return Builder.builder(CREATED, INTERNAL_SERVER_ERROR )
                .data(driver().runCypher(cypher,ontology))
                .successPredicate(objectContentResponse -> true)
                .compose();

    }

    @Override
    public ContentResponse<Object> runCypher(String cypher, String ontology, int pageSize, String cursorType) {
        return Builder.builder(CREATED, INTERNAL_SERVER_ERROR )
                .data(driver().runCypher(cypher,ontology,pageSize,cursorType))
                .successPredicate(objectContentResponse -> true)
                .compose();
    }

    @Override
    public ContentResponse<Object> runGraphQL(String graphQL, String ontology, int pageSize, String cursorType) {
        return Builder.builder(CREATED, INTERNAL_SERVER_ERROR )
                .data(driver().runGraphQL(graphQL,ontology,pageSize,cursorType))
                .successPredicate(objectContentResponse -> true)
                .compose();
    }
    @Override
    public ContentResponse<Object> runSparql(String sparql, String ontology, int pageSize, String cursorType) {
        return Builder.builder(CREATED, INTERNAL_SERVER_ERROR )
                .data(driver().runSparql(sparql,ontology,pageSize,cursorType))
                .successPredicate(objectContentResponse -> true)
                .compose();
    }

    @Override
    public ContentResponse<QueryResourceInfo> createAndFetch(CreateQueryRequest request) {
        return  createAndFetch(this.create(request),request);
    }

    public ContentResponse<QueryResourceInfo> createAndFetch(CreateJsonQueryRequest request) {
        return  createAndFetch(this.create(request),request);
    }

    private ContentResponse<QueryResourceInfo> createAndFetch(ContentResponse<QueryResourceInfo> queryResourceInfoResponse, CreateQueryRequestMetadata request) {
        if (queryResourceInfoResponse.status() == INTERNAL_SERVER_ERROR) {
            return Builder.<QueryResourceInfo>builder(CREATED, INTERNAL_SERVER_ERROR)
                    .data(Optional.of(queryResourceInfoResponse.getData()))
                    .successPredicate(response -> false)
                    .compose();
        }

        if (request.getCreateCursorRequest() == null) {
            return Builder.<QueryResourceInfo>builder(CREATED, INTERNAL_SERVER_ERROR)
                    .data(Optional.of(queryResourceInfoResponse.getData()))
                    .compose();
        }

        ContentResponse<CursorResourceInfo> cursorResourceInfoResponse =
                this.cursorController.create(queryResourceInfoResponse.getData().getResourceId(), request.getCreateCursorRequest());
        if (cursorResourceInfoResponse.status() == INTERNAL_SERVER_ERROR) {
            return Builder.<QueryResourceInfo>builder(CREATED, INTERNAL_SERVER_ERROR)
                    .data(Optional.of(new QueryResourceInfo(
                            queryResourceInfoResponse.getData().getType(),
                            queryResourceInfoResponse.getData().getResourceUrl(),
                            queryResourceInfoResponse.getData().getResourceId(),
                            queryResourceInfoResponse.getData().getCursorStoreUrl())))
                    .successPredicate(response -> false)
                    .compose();
        }

        if (request.getCreateCursorRequest().getCreatePageRequest() == null) {
            return Builder.<QueryResourceInfo>builder(CREATED, INTERNAL_SERVER_ERROR)
                    .data(Optional.of(new QueryResourceInfo(
                            queryResourceInfoResponse.getData().getType(),
                            queryResourceInfoResponse.getData().getResourceUrl(),
                            queryResourceInfoResponse.getData().getResourceId(),
                            queryResourceInfoResponse.getData().getCursorStoreUrl(),
                            cursorResourceInfoResponse.getData())))
                    .compose();
        }

//      early exist -> in case of parameterized query content already created
        if(queryResourceInfoResponse.getData()!=null &&
                !queryResourceInfoResponse.getData().getCursorResourceInfos().isEmpty() &&
                    !queryResourceInfoResponse.getData().getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty())
            return queryResourceInfoResponse;

        //
        ContentResponse<PageResourceInfo> pageResourceInfoResponse =
                this.pageController.create(
                        queryResourceInfoResponse.getData().getResourceId(),
                        cursorResourceInfoResponse.getData().getResourceId(),
                        request.getCreateCursorRequest().getCreatePageRequest());
        if (pageResourceInfoResponse.status() == INTERNAL_SERVER_ERROR) {
            return Builder.<QueryResourceInfo>builder(CREATED, INTERNAL_SERVER_ERROR)
                    .data(Optional.of(new QueryResourceInfo(
                            queryResourceInfoResponse.getData().getType(),
                            queryResourceInfoResponse.getData().getResourceUrl(),
                            queryResourceInfoResponse.getData().getResourceId(),
                            queryResourceInfoResponse.getData().getCursorStoreUrl(),
                            cursorResourceInfoResponse.getData())))
                    .successPredicate(response -> false)
                    .compose();
        }

        cursorResourceInfoResponse.getData().setPageResourceInfos(Collections.singletonList(pageResourceInfoResponse.getData()));

        ContentResponse<Object> pageDataResponse = this.pageController.getData(
                queryResourceInfoResponse.getData().getResourceId(),
                cursorResourceInfoResponse.getData().getResourceId(),
                pageResourceInfoResponse.getData().getResourceId());

        if (pageDataResponse.status() == INTERNAL_SERVER_ERROR) {
            return Builder.<QueryResourceInfo>builder(CREATED, INTERNAL_SERVER_ERROR)
                    .data(Optional.of(new QueryResourceInfo(
                            queryResourceInfoResponse.getData().getType(),
                            queryResourceInfoResponse.getData().getResourceUrl(),
                            queryResourceInfoResponse.getData().getResourceId(),
                            queryResourceInfoResponse.getData().getCursorStoreUrl(),
                            cursorResourceInfoResponse.getData())))
                    .successPredicate(response -> false)
                    .compose();
        }

        pageResourceInfoResponse.getData().setData(pageDataResponse.getData());
        cursorResourceInfoResponse.getData().setPageResourceInfos(Collections.singletonList(pageResourceInfoResponse.getData()));

        return Builder.<QueryResourceInfo>builder(CREATED, INTERNAL_SERVER_ERROR)
                .data(Optional.of(new QueryResourceInfo(
                        queryResourceInfoResponse.getData().getType(),
                        queryResourceInfoResponse.getData().getResourceUrl(),
                        queryResourceInfoResponse.getData().getResourceId(),
                        queryResourceInfoResponse.getData().getCursorStoreUrl(),
                        cursorResourceInfoResponse.getData())))
                .compose();

    }

    @Override
    public ContentResponse<QueryResourceInfo> callAndFetch(ExecuteStoredQueryRequest request) {
        Optional<QueryResourceInfo> queryResourceInfoResponse = driver().call(request);
        return Builder.<QueryResourceInfo>builder(CREATED, INTERNAL_SERVER_ERROR)
                .data(queryResourceInfoResponse)
                .compose();
    }

    @Override
    public ContentResponse<Object> fetchNextPage(String queryId, Optional<String> cursorId, int pageSize, boolean deleteCurrentPage) {
        return Builder.builder(OK, NOT_FOUND)
                .data(driver().getNextPageData(queryId,cursorId,pageSize,deleteCurrentPage))
                .compose();
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo() {
        return Builder.<StoreResourceInfo>builder(OK, NOT_FOUND)
                .data(driver().getInfo())
                .compose();
    }

    @Override
    public ContentResponse<QueryResourceInfo> getInfo(String queryId) {
        return Builder.<QueryResourceInfo>builder(OK, NOT_FOUND)
                .data(driver().getInfo(queryId))
                .compose();
    }

    @Override
    public ContentResponse<Query> getV1(String queryId) {
        return Builder.<Query>builder(OK, NOT_FOUND)
                .data(driver().getV1(queryId))
                .compose();
    }

    @Override
    public ContentResponse<AsgQuery> getAsg(String queryId) {
        return Builder.<AsgQuery>builder(OK, NOT_FOUND)
                .data(driver().getAsg(queryId))
                .compose();
    }

    @Override
    public ContentResponse<PlanNode<Plan>> planVerbose(String queryId) {
        return Builder.<PlanNode<Plan>>builder(OK, NOT_FOUND)
                .data(driver().planVerbose(queryId))
                .compose();
    }

    @Override
    public ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> explain(String queryId) {
        return Builder.<PlanWithCost<Plan, PlanDetailedCost>>builder(OK, NOT_FOUND)
                .data(driver().explain(queryId))
                .compose();
    }

    @Override
    public ContentResponse<Object> profile(String queryId) {
        return Builder.builder(OK, NOT_FOUND)
                .data(driver().profile(queryId))
                .compose();
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId) {
        return Builder.<Boolean>builder(ACCEPTED, NOT_FOUND)
                .data(driver().delete(queryId))
                .compose();
    }

    @Override
    public ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> plan(Query query) {
            return Builder.<PlanWithCost<Plan, PlanDetailedCost>>builder(ACCEPTED, NOT_FOUND)
                .data(driver().plan(query))
                .compose();
    }

    @Override
    public ContentResponse<GraphTraversal> traversal(Query query) {
        return Builder.<GraphTraversal>builder(ACCEPTED, NOT_FOUND)
                .data(driver().traversal(query))
                .compose();
    }
    //endregion

    /**
     * replace execution driver
     * @param driver
     * @return
     */
    public StandardQueryController driver(QueryDriver driver) {
        this.driver = driver;
        return this;
    }

    //region Fields
    private QueryDriver driver;
    private CursorController cursorController;
    private PageController pageController;
    //endregion
}
