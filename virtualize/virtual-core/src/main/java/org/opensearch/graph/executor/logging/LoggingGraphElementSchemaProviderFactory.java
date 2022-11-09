package org.opensearch.graph.executor.logging;

/*-
 * #%L
 * virtual-core
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





import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.unipop.schema.providers.GraphElementSchemaProvider;
import org.opensearch.graph.unipop.schema.providers.logging.LoggingGraphElementSchemaProvider;
import org.slf4j.Logger;

public class LoggingGraphElementSchemaProviderFactory implements GraphElementSchemaProviderFactory {
    public static final String schemaProviderFactoryParameter = "LoggingGraphElementSchemaProviderFactory.@schemaProviderFactory";
    public static final String warnLoggerParameter = "LoggingGraphElementSchemaProviderFactory.@warnLogger";
    public static final String verboseLoggerParameter = "LoggingGraphElementSchemaProviderFactory.@verboseLogger";

    //region Constructors
    @Inject
    public LoggingGraphElementSchemaProviderFactory(
            @Named(schemaProviderFactoryParameter) GraphElementSchemaProviderFactory schemaProviderFactory,
            @Named(warnLoggerParameter) Logger warnLogger,
            @Named(verboseLoggerParameter) Logger verboseLogger) {
        this.schemaProviderFactory = schemaProviderFactory;
        this.warnLogger = warnLogger;
        this.verboseLogger = verboseLogger;
    }
    //endregion

    //region GraphElementSchemaProviderFactory Implementation
    @Override
    public GraphElementSchemaProvider get(Ontology ontology) {
        GraphElementSchemaProvider schemaProvider = this.schemaProviderFactory.get(ontology);
        return new LoggingGraphElementSchemaProvider(schemaProvider, this.warnLogger, this.verboseLogger);
    }
    //endregion

    //region Fields
    private GraphElementSchemaProviderFactory schemaProviderFactory;

    private Logger warnLogger;
    private Logger verboseLogger;
    //endregion
}
