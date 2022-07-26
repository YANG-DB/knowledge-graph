package org.opensearch.graph.services.controllers.logging;




import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.opensearch.graph.dispatcher.logging.*;
import org.opensearch.graph.services.suppliers.RequestExternalMetadataSupplier;
import org.opensearch.graph.services.suppliers.RequestIdSupplier;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.services.controllers.CatalogController;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.opensearch.graph.dispatcher.logging.LogMessage.Level.info;
import static org.opensearch.graph.dispatcher.logging.LogMessage.Level.trace;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingCatalogController extends LoggingControllerBase<CatalogController> implements CatalogController {
    public static final String controllerParameter = "LoggingCatalogController.@controller";
    public static final String loggerParameter = "LoggingCatalogController.@logger";

    //region Constructors
    @Inject
    public LoggingCatalogController(
            @Named(controllerParameter) CatalogController controller,
            @Named(loggerParameter) Logger logger,
            RequestIdSupplier requestIdSupplier,
            RequestExternalMetadataSupplier requestExternalMetadataSupplier,
            MetricRegistry metricRegistry) {
        super(controller, logger, requestIdSupplier, requestExternalMetadataSupplier, metricRegistry);
    }
    //endregion

    //region CatalogController Implementation
    @Override
    public ContentResponse<Ontology> getOntology(String id) {
        return new LoggingSyncMethodDecorator<ContentResponse<Ontology>>(
                this.logger,
                this.metricRegistry,
                getOntology,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.getOntology(id), this.resultHandler());
    }

    @Override
    public ContentResponse<Ontology> addOntology(Ontology ontology) {
        return new LoggingSyncMethodDecorator<ContentResponse<Ontology>>(
                this.logger,
                this.metricRegistry,
                addOntology,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.addOntology(ontology), this.resultHandler());

    }

    @Override
    public ContentResponse<List<Ontology>> getOntologies() {
        return new LoggingSyncMethodDecorator<ContentResponse<List<Ontology>>>(
                this.logger,
                this.metricRegistry,
                getOntologies,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.getOntologies(), this.resultHandler());
    }

    @Override
    public ContentResponse<String> getSchema(String id) {
        return new LoggingSyncMethodDecorator<ContentResponse<String>>(
                this.logger,
                this.metricRegistry,
                getSchema,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.getSchema(id), this.resultHandler());
    }

    @Override
    public ContentResponse<List<String>> getSchemas() {
        return new LoggingSyncMethodDecorator<ContentResponse<List<String>>>(
                this.logger,
                this.metricRegistry,
                getSchemas,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.getSchemas(), this.resultHandler());
    }
    //endregion

    //region Fields
    private static MethodName.MDCWriter getOntology = MethodName.of("getOntology");
    private static MethodName.MDCWriter getOntologies = MethodName.of("getOntologies");
    private static MethodName.MDCWriter addOntology = MethodName.of("addOntology");
    private static MethodName.MDCWriter getSchema = MethodName.of("getSchema");
    private static MethodName.MDCWriter getSchemas = MethodName.of("getSchemas");
    //endregion
}
