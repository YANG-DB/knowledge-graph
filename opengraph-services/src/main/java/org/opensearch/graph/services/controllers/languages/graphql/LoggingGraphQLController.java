package org.opensearch.graph.services.controllers.languages.graphql;

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
import org.opensearch.graph.dispatcher.logging.LoggingSyncMethodDecorator;
import org.opensearch.graph.dispatcher.logging.MethodName;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.services.controllers.SchemaTranslatorController;
import org.opensearch.graph.services.controllers.logging.LoggingControllerBase;
import org.opensearch.graph.services.suppliers.RequestExternalMetadataSupplier;
import org.opensearch.graph.services.suppliers.RequestIdSupplier;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collections;

import static org.opensearch.graph.dispatcher.logging.LogMessage.Level.info;
import static org.opensearch.graph.dispatcher.logging.LogMessage.Level.trace;

public class LoggingGraphQLController extends LoggingControllerBase<SchemaTranslatorController> implements SchemaTranslatorController {
    public static final String controllerParameter = "LoggingGraphQLController.@controller";
    public static final String loggerParameter = "LoggingGraphQLController.@logger";

    //region Constructors
    @Inject
    public LoggingGraphQLController(
            @Named(controllerParameter) SchemaTranslatorController controller,
            @Named(loggerParameter) Logger logger,
            RequestIdSupplier requestIdSupplier,
            RequestExternalMetadataSupplier requestExternalMetadataSupplier,
            MetricRegistry metricRegistry) {
        super(controller, logger, requestIdSupplier, requestExternalMetadataSupplier, metricRegistry);
    }
    //endregion

    //region CatalogController Implementation
    @Override
    public ContentResponse<Ontology> translate(String ontology, String graphqlschema) {
        return new LoggingSyncMethodDecorator<ContentResponse<Ontology>>(
                this.logger,
                this.metricRegistry,
                translate,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.translate(ontology, graphqlschema), this.resultHandler());
    }

    @Override
    public ContentResponse<String> transform(String ontologyId) {
        return new LoggingSyncMethodDecorator<ContentResponse<String>>(
                this.logger,
                this.metricRegistry,
                translate,
                this.primerMdcWriter(),
                Collections.singletonList(trace),
                Arrays.asList(info, trace))
                .decorate(() -> this.controller.transform(ontologyId), this.resultHandler());
    }

    //endregion

    //region Fields
    private static MethodName.MDCWriter translate = MethodName.of("translate");
    //endregion
}
