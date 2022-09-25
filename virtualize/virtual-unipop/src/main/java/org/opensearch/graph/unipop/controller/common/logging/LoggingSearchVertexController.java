package org.opensearch.graph.unipop.controller.common.logging;

/*-
 * #%L
 * virtual-unipop
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
import org.opensearch.graph.dispatcher.decorators.MethodDecorator.ResultHandler.Passthrough;
import org.opensearch.graph.dispatcher.logging.*;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unipop.process.Profiler;
import org.unipop.query.search.SearchVertexQuery;

import java.util.Collections;
import java.util.Iterator;

import static com.codahale.metrics.MetricRegistry.name;
import static org.opensearch.graph.dispatcher.logging.LogMessage.Level.trace;

public class LoggingSearchVertexController implements SearchVertexQuery.SearchVertexController {
    //region Constructors
    public LoggingSearchVertexController(
            SearchVertexQuery.SearchVertexController searchVertexController,
            MetricRegistry metricRegistry) {
        this.logger = LoggerFactory.getLogger(searchVertexController.getClass());
        this.metricRegistry = metricRegistry;
        this.searchVertexController = searchVertexController;
    }
    //endregion

    //region SearchVertexQuery.SearchVertexController Implementation
    @Override
    public Iterator<Edge> search(SearchVertexQuery searchVertexQuery) {
        return new LoggingSyncMethodDecorator<Iterator<Edge>>(this.logger, this.metricRegistry, search, trace)
                .decorate(() -> this.searchVertexController.search(searchVertexQuery), new Passthrough<>((ex) -> Collections.emptyIterator()));
    }

    @Override
    public Profiler getProfiler() {
        return this.searchVertexController.getProfiler();
    }

    @Override
    public void setProfiler(Profiler profiler) {
        this.searchVertexController.setProfiler(profiler);
    }
    //endregion

    //region Fields
    private Logger logger;
    private MetricRegistry metricRegistry;
    private SearchVertexQuery.SearchVertexController searchVertexController;

    private static MethodName.MDCWriter search = MethodName.of("search");
    //endregion
}
