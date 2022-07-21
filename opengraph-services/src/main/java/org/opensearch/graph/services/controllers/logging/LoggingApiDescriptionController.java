package org.opensearch.graph.services.controllers.logging;


import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.opensearch.graph.dispatcher.logging.*;
import org.opensearch.graph.services.suppliers.RequestExternalMetadataSupplier;
import org.opensearch.graph.services.suppliers.RequestIdSupplier;
import org.opensearch.graph.model.resourceInfo.FuseResourceInfo;
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
    public ContentResponse<FuseResourceInfo> getInfo() {
        return new LoggingSyncMethodDecorator<ContentResponse<FuseResourceInfo>>(
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
