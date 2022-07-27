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
import org.opensearch.graph.dispatcher.logging.*;
import org.opensearch.graph.services.suppliers.RequestExternalMetadataSupplier;
import org.opensearch.graph.services.suppliers.RequestIdSupplier;
import org.opensearch.graph.model.resourceInfo.GraphResourceInfo;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.services.controllers.ApiDescriptionController;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collections;

import static org.opensearch.graph.dispatcher.logging.LogMessage.Level.*;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingApiDescriptionController extends LoggingControllerBase<ApiDescriptionController> implements ApiDescriptionController {
    public static final String controllerParameter = "LoggingApiDescriptionController.@controller";
    public static final String loggerParameter = "LoggingApiDescriptionController.@logger";

    //region Constructors
    @Inject
    public LoggingApiDescriptionController(
            @Named(controllerParameter) ApiDescriptionController controller,
            @Named(loggerParameter) Logger logger,
            RequestIdSupplier requestIdSupplier,
            RequestExternalMetadataSupplier requestExternalMetadataSupplier,
            MetricRegistry metricRegistry) {
        super(controller, logger, requestIdSupplier, requestExternalMetadataSupplier, metricRegistry);
    }
    //endregion

    //region ApiDescriptionController Implementation
    @Override
    public ContentResponse<GraphResourceInfo> getInfo() {
        return new LoggingSyncMethodDecorator<ContentResponse<GraphResourceInfo>>(
                this.logger,
                this.metricRegistry,
                getInfo,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.getInfo(), this.resultHandler());
    }
    //endregion

    //region Fields
    private static MethodName.MDCWriter getInfo = MethodName.of("getInfo");
    //endregion
}
