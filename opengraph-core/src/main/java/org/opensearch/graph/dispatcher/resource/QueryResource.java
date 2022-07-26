package org.opensearch.graph.dispatcher.resource;







import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.planTree.PlanNode;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.query.QueryMetadata;
import org.opensearch.graph.model.transport.CreateQueryRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class QueryResource {
    //region Constructors
    public QueryResource(CreateQueryRequest request, Query query, AsgQuery asgQuery, QueryMetadata queryMetadata, PlanWithCost<Plan, PlanDetailedCost> executionPlan) {
        this(request, query, asgQuery, queryMetadata, executionPlan, Optional.empty());
    }

    public QueryResource(CreateQueryRequest request, Query query, AsgQuery asgQuery, QueryMetadata queryMetadata, PlanWithCost<Plan, PlanDetailedCost> executionPlan, Optional<PlanNode<Plan>> planNode) {
        this.request = request;
        this.query = query;
        this.asgQuery = asgQuery;
        this.queryMetadata = queryMetadata;
        this.planNode = planNode;
        this.cursorResources = new HashMap<>();
        this.innerQueryResources = new HashMap<>();
        this.executionPlan = executionPlan;
    }
    //endregion

    //region Public Methods
    public QueryResource withInnerQueryResources(List<QueryResource> resources) {
        resources.forEach(this::addInnerQueryResource);
        return this;
    }

    public void addInnerQueryResource(QueryResource resource) {
        this.innerQueryResources.put(resource.getQueryMetadata().getId(), resource);
    }

    public Iterable<QueryResource> getInnerQueryResources() {
        return this.innerQueryResources.values();
    }

    public void addCursorResource(String cursorId, CursorResource cursorResource) {
        this.cursorResources.put(cursorId, cursorResource);
    }

    public Iterable<CursorResource> getCursorResources() {
        return this.cursorResources.values();
    }

    public Optional<CursorResource> getCursorResource(String cursorId) {
        return Optional.ofNullable(this.cursorResources.get(cursorId));
    }

    //endregion

    public void deleteCursorResource(String cursorId) {
        this.cursorResources.remove(cursorId);
    }

    public String getNextCursorId() {
        return String.valueOf(this.cursorSequence.incrementAndGet());
    }

    public String getCurrentCursorId() {
        return String.valueOf(this.cursorSequence.get());
    }

    //region Properties
    public Query getQuery() {
        return this.query;
    }

    public AsgQuery getAsgQuery() {
        return this.asgQuery;
    }

    public QueryMetadata getQueryMetadata() {
        return queryMetadata;
    }

    public PlanWithCost<Plan, PlanDetailedCost> getExecutionPlan() {
        return this.executionPlan;
    }

    public CreateQueryRequest getRequest() {
        return request;
    }

    public Optional<PlanNode<Plan>> getPlanNode() {
        return planNode;
    }
    //endregion

    //region Fields
    private Query query;
    private CreateQueryRequest request;
    private QueryMetadata queryMetadata;
    private PlanWithCost<Plan, PlanDetailedCost> executionPlan;
    private Optional<PlanNode<Plan>> planNode;
    private AsgQuery asgQuery;
    private Map<String, CursorResource> cursorResources;
    private Map<String, QueryResource> innerQueryResources;
    private AtomicInteger cursorSequence = new AtomicInteger();
    //endregion
}
