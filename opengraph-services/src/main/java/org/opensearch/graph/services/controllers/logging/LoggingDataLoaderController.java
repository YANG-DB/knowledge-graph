package org.opensearch.graph.services.controllers.logging;




import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.opensearch.graph.dispatcher.logging.*;
import org.opensearch.graph.executor.ontology.schema.load.GraphDataLoader;
import org.opensearch.graph.model.results.LoadResponse;
import org.opensearch.graph.model.logical.LogicalGraphModel;
import org.opensearch.graph.model.resourceInfo.GraphError;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.services.controllers.DataLoaderController;
import org.opensearch.graph.services.suppliers.RequestExternalMetadataSupplier;
import org.opensearch.graph.services.suppliers.RequestIdSupplier;
import org.slf4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import static com.codahale.metrics.MetricRegistry.name;
import static org.opensearch.graph.dispatcher.logging.LogMessage.Level.*;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingDataLoaderController extends LoggingControllerBase<DataLoaderController> implements DataLoaderController {
    public static final String controllerParameter = "LoggingDataLoaderController.@controller";
    public static final String loggerParameter = "LoggingDataLoaderController.@logger";

    //region Constructors
    @Inject
    public LoggingDataLoaderController(
            @Named(controllerParameter) DataLoaderController controller,
            @Named(loggerParameter) Logger logger,
            RequestIdSupplier requestIdSupplier,
            RequestExternalMetadataSupplier requestExternalMetadataSupplier,
            MetricRegistry metricRegistry) {
        super(controller, logger, requestIdSupplier, requestExternalMetadataSupplier, metricRegistry);
    }
    //endregion

    //region CatalogController Implementation
    @Override
    public ContentResponse<LoadResponse<String, GraphError>> loadGraph(String ontology, LogicalGraphModel data, GraphDataLoader.Directive directive) {
        return new LoggingSyncMethodDecorator<ContentResponse<LoadResponse<String, GraphError>>>(
                this.logger,
                this.metricRegistry,
                load,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.loadGraph(ontology, data,directive ), this.resultHandler());
    }

    @Override
    public ContentResponse<LoadResponse<String, GraphError>> loadCsv(String ontology, String type, String label, String data, GraphDataLoader.Directive directive) {
        return new LoggingSyncMethodDecorator<ContentResponse<LoadResponse<String, GraphError>>>(
                this.logger,
                this.metricRegistry,
                load,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.loadCsv(ontology,type, label, data, directive), this.resultHandler());
    }

    @Override
    public ContentResponse<LoadResponse<String, GraphError>> loadGraph(String ontology, File data, GraphDataLoader.Directive directive) {
        return new LoggingSyncMethodDecorator<ContentResponse<LoadResponse<String, GraphError>>>(
                this.logger,
                this.metricRegistry,
                load,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.loadGraph(ontology, data,directive ), this.resultHandler());
    }

    @Override
    public ContentResponse<LoadResponse<String, GraphError>> loadCsv(String ontology, String type, String label, File data, GraphDataLoader.Directive directive) {
        return new LoggingSyncMethodDecorator<ContentResponse<LoadResponse<String, GraphError>>>(
                this.logger,
                this.metricRegistry,
                load,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.loadCsv(ontology,type,label , data, directive), this.resultHandler());
    }

    @Override
    public ContentResponse<String> init(String ontology ) {
        return new LoggingSyncMethodDecorator<ContentResponse<String>>(
                this.logger,
                this.metricRegistry,
                init,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.init(ontology), this.resultHandler());
    }
    @Override
    public ContentResponse<String> createMapping(String ontology) {
        return new LoggingSyncMethodDecorator<ContentResponse<String>>(
                this.logger,
                this.metricRegistry,
                init,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.createMapping(ontology), this.resultHandler());
    }
    @Override
    public ContentResponse<String> createIndices(String ontology) {
        return new LoggingSyncMethodDecorator<ContentResponse<String>>(
                this.logger,
                this.metricRegistry,
                init,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.createIndices(ontology), this.resultHandler());
    }

    @Override
    public ContentResponse<String> drop(String ontology ) {
        return new LoggingSyncMethodDecorator<ContentResponse<String>>(
                this.logger,
                this.metricRegistry,
                drop,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.drop(ontology), this.resultHandler());
    }
    //endregion

    //region Fields
    private static MethodName.MDCWriter load = MethodName.of("load");
    private static MethodName.MDCWriter init = MethodName.of("init");
    private static MethodName.MDCWriter drop = MethodName.of("drop");
    //endregion
}
