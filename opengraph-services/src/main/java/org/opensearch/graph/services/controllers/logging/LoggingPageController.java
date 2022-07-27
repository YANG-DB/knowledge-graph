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
import org.opensearch.graph.dispatcher.driver.PageDriver;
import org.opensearch.graph.dispatcher.logging.*;
import org.opensearch.graph.dispatcher.logging.LogMessage.MDCWriter.Composite;
import org.opensearch.graph.model.resourceInfo.PageResourceInfo;
import org.opensearch.graph.model.resourceInfo.StoreResourceInfo;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.model.transport.CreatePageRequest;
import org.opensearch.graph.model.transport.cursor.LogicalGraphCursorRequest;
import org.opensearch.graph.services.controllers.PageController;
import org.opensearch.graph.services.suppliers.RequestExternalMetadataSupplier;
import org.opensearch.graph.services.suppliers.RequestIdSupplier;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collections;

import static org.opensearch.graph.dispatcher.logging.LogMessage.Level.*;
import static org.opensearch.graph.dispatcher.logging.RequestIdByScope.Builder.query;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingPageController extends LoggingControllerBase<PageController> implements PageController<PageController,PageDriver>  {
    public static final String controllerParameter = "LoggingPageController.@controller";
    public static final String loggerParameter = "LoggingPageController.@logger";

    //region Constructors
    @Inject
    public LoggingPageController(
            @Named(controllerParameter) PageController controller,
            @Named(loggerParameter) Logger logger,
            RequestIdSupplier requestIdSupplier,
            RequestExternalMetadataSupplier requestExternalMetadataSupplier,
            MetricRegistry metricRegistry) {
        super(controller, logger, requestIdSupplier, requestExternalMetadataSupplier, metricRegistry);
    }
    //endregion

    //region PageController Implementation
    @Override
    public ContentResponse<PageResourceInfo> create(String queryId, String cursorId, CreatePageRequest createPageRequest) {
        return new LoggingSyncMethodDecorator<ContentResponse<PageResourceInfo>>(
                this.logger,
                this.metricRegistry,
                create,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query(queryId).cursor(cursorId).get())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.create(queryId, cursorId, createPageRequest), this.resultHandler());
    }

    @Override
    public ContentResponse<PageResourceInfo> createAndFetch(String queryId, String cursorId, CreatePageRequest createPageRequest) {
        return new LoggingSyncMethodDecorator<ContentResponse<PageResourceInfo>>(
                this.logger,
                this.metricRegistry,
                createAndFetch,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query(queryId).cursor(cursorId).get())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.createAndFetch(queryId, cursorId, createPageRequest), this.resultHandler());
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo(String queryId, String cursorId) {
        return new LoggingSyncMethodDecorator<ContentResponse<StoreResourceInfo>>(
                this.logger,
                this.metricRegistry,
                getInfoByQueryIdAndCursorId,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query(queryId).cursor(cursorId).get())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.getInfo(queryId, cursorId), this.resultHandler());
    }

    @Override
    public ContentResponse<PageResourceInfo> getInfo(String queryId, String cursorId, String pageId) {
        return new LoggingSyncMethodDecorator<ContentResponse<PageResourceInfo>>(
                this.logger,
                this.metricRegistry,
                getInfoByQueryIdAndCursorIdAndPageId,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query(queryId).cursor(cursorId).page(pageId).get())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.getInfo(queryId, cursorId, pageId), this.resultHandler());
    }

    @Override
    public ContentResponse<Object> getData(String queryId, String cursorId, String pageId) {
        return new LoggingSyncMethodDecorator<ContentResponse<Object>>(
                this.logger,
                this.metricRegistry,
                getData,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query(queryId).cursor(cursorId).page(pageId).get())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.getData(queryId, cursorId, pageId), this.resultHandler());
    }

    @Override
    public ContentResponse<Object> format(String queryId, String cursorId, String pageId, LogicalGraphCursorRequest.GraphFormat format) {
        return new LoggingSyncMethodDecorator<ContentResponse<Object>>(
                this.logger,
                this.metricRegistry,
                getData,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query(queryId).cursor(cursorId).page(pageId).get())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.format(queryId, cursorId, pageId,format), this.resultHandler());
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId, String cursorId, String pageId) {
        return new LoggingSyncMethodDecorator<ContentResponse<Boolean>>(
                this.logger,
                this.metricRegistry,
                delete,
                Composite.of(this.primerMdcWriter(), RequestIdByScope.of(query(queryId).cursor(cursorId).page(pageId).get())),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.delete(queryId, cursorId, pageId), this.resultHandler());
    }
    //endregion

    //region Fields
    private static MethodName.MDCWriter create = MethodName.of("create");
    private static MethodName.MDCWriter createAndFetch = MethodName.of("createAndFetch");
    private static MethodName.MDCWriter getInfoByQueryIdAndCursorId = MethodName.of("getInfoByQueryIdAndCursorId");
    private static MethodName.MDCWriter getInfoByQueryIdAndCursorIdAndPageId = MethodName.of("getInfoByQueryIdAndCursorIdAndPageId");
    private static MethodName.MDCWriter getData = MethodName.of("getData");
    private static MethodName.MDCWriter delete = MethodName.of("delete");

    @Override
    public PageController driver(PageDriver driver) {
        return (PageController) this.controller.driver(driver);
    }
    //endregion
}
