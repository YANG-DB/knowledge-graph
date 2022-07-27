package org.opensearch.graph.services.controllers.logging;

/*-
 * #%L
 * opengraph-services
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




import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.opensearch.graph.dispatcher.driver.QueryDriver;
import org.opensearch.graph.dispatcher.logging.*;
import org.opensearch.graph.dispatcher.logging.LogMessage.MDCWriter.Composite;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.descriptors.Descriptor;
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
import org.opensearch.graph.services.suppliers.RequestExternalMetadataSupplier;
import org.opensearch.graph.services.suppliers.RequestIdSupplier;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.opensearch.graph.dispatcher.logging.LogMessage.Level.*;
import static org.opensearch.graph.dispatcher.logging.LogType.*;
import static org.opensearch.graph.dispatcher.logging.RequestIdByScope.Builder.query;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingQueryController extends LoggingControllerBase<QueryController> implements QueryController<QueryController,QueryDriver> {
    public static final String controllerParameter = "LoggingQueryController.@controller";
    public static final String loggerParameter = "LoggingQueryController.@logger";
    public static final String queryDescriptorParameter = "LoggingQueryController.@queryDescriptor";

    //region Constructors
    @Inject
    public LoggingQueryController(
            @Named(controllerParameter) QueryController controller,
            @Named(loggerParameter) Logger logger,
            @Named(queryDescriptorParameter) Descriptor<Query> queryDescriptor,
            RequestIdSupplier requestIdSupplier,
            RequestExternalMetadataSupplier requestExternalMetadataSupplier,
            MetricRegistry metricRegistry) {
        super(controller, logger, requestIdSupplier, requestExternalMetadataSupplier, metricRegistry);
        this.queryDescriptor = queryDescriptor;
    }
    //endregion

    //region QueryController Implementation
    @Override
    public ContentResponse<QueryResourceInfo> create(CreateQueryRequest request) {
        return new LoggingSyncMethodDecorator<ContentResponse<QueryResourceInfo>>(
                this.logger,
                this.metricRegistry,
                create,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> {
                    if (request.getQuery() != null) {
                        new LogMessage.Impl(this.logger, debug, "query: {}", Sequence.incr(), LogType.of(log), create)
                                .with(this.queryDescriptor.describe(request.getQuery())).log();
                    }
                    return this.controller.create(request);
                }, this.resultHandler());
    }

    @Override
    public ContentResponse<QueryResourceInfo> create(CreateJsonQueryRequest request) {
        return new LoggingSyncMethodDecorator<ContentResponse<QueryResourceInfo>>(
                this.logger,
                this.metricRegistry,
                create,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> {
                    if (request.getQuery() != null) {
                        new LogMessage.Impl(this.logger, debug, "query: {}", Sequence.incr(), LogType.of(log), create)
                                .with(request.getQuery()).log();
                    }
                    return this.controller.create(request);
                }, this.resultHandler());
    }

    @Override
    public ContentResponse<Object> runV1Query(Query query, int pageSize, String cursorType) {
        return new LoggingSyncMethodDecorator<ContentResponse<Object>>(
                this.logger,
                this.metricRegistry,
                run,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> {
                    if (query != null) {
                        new LogMessage.Impl(this.logger, debug, "query: {}", Sequence.incr(), LogType.of(log), createAndFetch)
                                .with(this.queryDescriptor.describe(query)).log();
                    }
                    return this.controller.runV1Query(query, pageSize, cursorType);
                }, this.resultHandler());
    }

    @Override
    public ContentResponse<ValidationResult> validate(Query query) {
        return new LoggingSyncMethodDecorator<ContentResponse<ValidationResult>>(
                this.logger,
                this.metricRegistry,
                validate,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> {
                    if (query != null) {
                        new LogMessage.Impl(this.logger, debug, "query: {}", Sequence.incr(), LogType.of(log), createAndFetch)
                                .with(this.queryDescriptor.describe(query)).log();
                    }
                    return this.controller.validate(query);
                }, this.resultHandler());
    }

    @Override
    public ContentResponse<Object> getVertex(String ontology, String type, String id) {
        return new LoggingSyncMethodDecorator<ContentResponse<Object>>(
                this.logger,
                this.metricRegistry,
                validate,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> {
                    new LogMessage.Impl(this.logger, debug, String.format("getVertex: [%s,%s] { }",
                            ontology,type,id), Sequence.incr(), LogType.of(log), createAndFetch)
                            .log();
                    return this.controller.getVertex(ontology,type,id);
                }, this.resultHandler());
    }

    @Override
    public ContentResponse<Object> getNeighbors(String ontology, String type, String id) {
        return new LoggingSyncMethodDecorator<ContentResponse<Object>>(
                this.logger,
                this.metricRegistry,
                validate,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> {
                    new LogMessage.Impl(this.logger, debug, String.format("getNeighbors: [%s,%s] { }",
                            ontology,type,id), Sequence.incr(), LogType.of(log), createAndFetch)
                            .log();
                    return this.controller.getNeighbors(ontology,type,id);
                }, this.resultHandler());
    }

    @Override
    public ContentResponse<Object> findPath(String ontology, String sourceEntity, String sourceId, String targetEntity,String targetId, String relationType, int maxHops){
        return new LoggingSyncMethodDecorator<ContentResponse<Object>>(
                this.logger,
                this.metricRegistry,
                validate,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> {
                        new LogMessage.Impl(this.logger, debug, String.format("findPath: [%s,%s,%s,%s,%s,%s,%d] { }",
                                ontology,sourceEntity,sourceId,targetEntity,targetId,relationType,maxHops), Sequence.incr(), LogType.of(log), createAndFetch)
                                .log();
                    return this.controller.findPath(ontology,sourceEntity,sourceId,targetEntity,targetId,relationType,maxHops);
                }, this.resultHandler());
    }

    @Override
    public ContentResponse<Object> runCypher(String cypher, String ontology) {
        return new LoggingSyncMethodDecorator<ContentResponse<Object>>(
                this.logger,
                this.metricRegistry,
                run,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> {
                    if (cypher != null) {
                        new LogMessage.Impl(this.logger, debug, "query: {}", Sequence.incr(), LogType.of(log), createAndFetch)
                                .with(cypher).log();
                    }
                    return this.controller.runCypher(cypher,ontology );
                }, this.resultHandler());
    }

    @Override
    public ContentResponse<Object> runCypher(String cypher, String ontology, int pageSize, String cursorType) {
        return new LoggingSyncMethodDecorator<ContentResponse<Object>>(
                this.logger,
                this.metricRegistry,
                run,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> {
                    if (cypher != null) {
                        new LogMessage.Impl(this.logger, debug, "query: {}", Sequence.incr(), LogType.of(log), createAndFetch)
                                .with(cypher).log();
                    }
                    return this.controller.runCypher(cypher,ontology , pageSize, cursorType);
                }, this.resultHandler());

    }

    @Override
    public ContentResponse<Object> runGraphQL(String graphQL, String ontology, int pageSize, String cursorType) {
        return new LoggingSyncMethodDecorator<ContentResponse<Object>>(
                this.logger,
                this.metricRegistry,
                run,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> {
                    if (graphQL != null) {
                        new LogMessage.Impl(this.logger, debug, "query: {}", Sequence.incr(), LogType.of(log), createAndFetch)
                                .with(graphQL).log();
                    }
                    return this.controller.runGraphQL(graphQL,ontology , pageSize, cursorType);
                }, this.resultHandler());
    }

/*
    @Override
    public ContentResponse<Object> runSparql(String sparql, String ontology, int pageSize, String cursorType) {
        return new LoggingSyncMethodDecorator<ContentResponse<Object>>(
                this.logger,
                this.metricRegistry,
                run,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> {
                    if (sparql != null) {
                        new LogMessage.Impl(this.logger, debug, "query: {}", Sequence.incr(), LogType.of(log), createAndFetch)
                                .with(sparql).log();
                    }
                    return this.controller.runSparql(sparql,ontology , pageSize, cursorType);
                }, this.resultHandler());
    }
*/

    @Override
    public ContentResponse<QueryResourceInfo> createAndFetch(CreateQueryRequest request) {
        return new LoggingSyncMethodDecorator<ContentResponse<QueryResourceInfo>>(
                this.logger,
                this.metricRegistry,
                createAndFetch,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> {
                    if (request.getQuery() != null) {
                        new LogMessage.Impl(this.logger, debug, "query: {}", Sequence.incr(), LogType.of(log), createAndFetch)
                                .with(this.queryDescriptor.describe(request.getQuery())).log();
                    }
                    return this.controller.createAndFetch(request);
                }, this.resultHandler());
    }

    @Override
    public ContentResponse<QueryResourceInfo> createAndFetch(CreateJsonQueryRequest request) {
        return new LoggingSyncMethodDecorator<ContentResponse<QueryResourceInfo>>(
                this.logger,
                this.metricRegistry,
                createAndFetch,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> {
                    if (request.getQuery() != null) {
                        new LogMessage.Impl(this.logger, debug, "query: {}", Sequence.incr(), LogType.of(log), createAndFetch)
                                .with(request.getQuery()).log();
                    }
                    return this.controller.createAndFetch(request);
                }, this.resultHandler());
    }

    @Override
    public ContentResponse<QueryResourceInfo> callAndFetch(ExecuteStoredQueryRequest request) {
        return new LoggingSyncMethodDecorator<ContentResponse<QueryResourceInfo>>(
                this.logger,
                this.metricRegistry,
                callAndFetch,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> {
                    if (request.getQuery() != null) {
                        new LogMessage.Impl(this.logger, debug, "query: {}", Sequence.incr(), LogType.of(log), createAndFetch)
                                .with(this.queryDescriptor.describe(request.getQuery())).log();
                    }
                    return this.controller.callAndFetch(request);
                }, this.resultHandler());
    }

    @Override
    public ContentResponse<Object> fetchNextPage(String queryId, Optional<String> cursorId, int pageSize, boolean deleteCurrentPage) {
        return new LoggingSyncMethodDecorator<ContentResponse<Object>>(
                this.logger,
                this.metricRegistry,
                fetchNextPage,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.fetchNextPage(queryId,cursorId,pageSize,deleteCurrentPage), this.resultHandler());
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo() {
        return new LoggingSyncMethodDecorator<ContentResponse<StoreResourceInfo>>(
                this.logger,
                this.metricRegistry,
                getInfoAll,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.getInfo(), this.resultHandler());
    }

    @Override
    public ContentResponse<QueryResourceInfo> getInfo(String queryId) {
        return new LoggingSyncMethodDecorator<ContentResponse<QueryResourceInfo>>(
                this.logger,
                this.metricRegistry,
                getInfo,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query(queryId).get())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.getInfo(queryId), this.resultHandler());
    }

    @Override
    public ContentResponse<Query> getV1(String queryId) {
        return new LoggingSyncMethodDecorator<ContentResponse<Query>>(
                this.logger,
                this.metricRegistry,
                getV1ByQueryId,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query(queryId).get())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.getV1(queryId), this.resultHandler());
    }

    @Override
    public ContentResponse<AsgQuery> getAsg(String queryId) {
        return new LoggingSyncMethodDecorator<ContentResponse<AsgQuery>>(
                this.logger,
                this.metricRegistry,
                getAsgByQueryId,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query(queryId).get())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.getAsg(queryId), this.resultHandler());
    }

    @Override
    public ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> explain(String queryId) {
        return new LoggingSyncMethodDecorator<ContentResponse<PlanWithCost<Plan, PlanDetailedCost>>>(
                this.logger,
                this.metricRegistry,
                explain,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query(queryId).get())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.explain(queryId), this.resultHandler());
    }

    @Override
    public ContentResponse<Object> profile(String queryId) {
        return new LoggingSyncMethodDecorator<ContentResponse<Object>>(
                this.logger,
                this.metricRegistry,
                profile,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query(queryId).get())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.profile(queryId), this.resultHandler());

    }

    @Override
    public ContentResponse<PlanNode<Plan>> planVerbose(String queryId) {
        return new LoggingSyncMethodDecorator<ContentResponse<PlanNode<Plan>>>(
                this.logger,
                this.metricRegistry,
                planVerbose,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query(queryId).get())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.planVerbose(queryId), this.resultHandler());
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId) {
        return new LoggingSyncMethodDecorator<ContentResponse<Boolean>>(
                this.logger,
                this.metricRegistry,
                delete,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query(queryId).get())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.delete(queryId), this.resultHandler());
    }

    @Override
    public ContentResponse<PlanWithCost<Plan, PlanDetailedCost>> plan(Query query) {
        return new LoggingSyncMethodDecorator<ContentResponse<PlanWithCost<Plan, PlanDetailedCost>>>(
                this.logger,
                this.metricRegistry,
                plan,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query.getName())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.plan(query), this.resultHandler());
    }

    @Override
    public ContentResponse<GraphTraversal> traversal(Query query) {
        return new LoggingSyncMethodDecorator<ContentResponse<GraphTraversal>>(
                this.logger,
                this.metricRegistry,
                traversal,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query.getName())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.traversal(query), this.resultHandler());
    }


    @Override
    public QueryController driver(QueryDriver driver) {
        return (QueryController) this.controller.driver(driver);
    }
    //endregion

    //region Fields
    private Descriptor<Query> queryDescriptor;

    private static MethodName.MDCWriter create = MethodName.of("create");
    private static MethodName.MDCWriter createAndFetch = MethodName.of("createAndFetch");
    private static MethodName.MDCWriter getInfo = MethodName.of("getInfo");
    private static MethodName.MDCWriter getV1ByQueryId = MethodName.of("getV1ByQueryId");
    private static MethodName.MDCWriter getAsgByQueryId = MethodName.of("getAsgByQueryId");
    private static MethodName.MDCWriter traversal = MethodName.of("traversal");
    private static MethodName.MDCWriter explain = MethodName.of("explain");
    private static MethodName.MDCWriter profile = MethodName.of("profile");
    private static MethodName.MDCWriter planVerbose = MethodName.of("planVerbose");
    private static MethodName.MDCWriter delete = MethodName.of("delete");
    private static MethodName.MDCWriter plan = MethodName.of("plan");
    private static MethodName.MDCWriter validate = MethodName.of("validate");
    private static MethodName.MDCWriter run = MethodName.of("run");
    private static MethodName.MDCWriter callAndFetch = MethodName.of("callAndFetch");
    private static MethodName.MDCWriter getInfoAll = MethodName.of("getInfoAll");
    private static MethodName.MDCWriter fetchNextPage = MethodName.of("fetchNextPage");

    private static LogMessage.MDCWriter sequence = Sequence.incr();

    //endregion
}
