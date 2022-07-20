package org.opensearch.graph.unipop.controller.common.logging;

/*-
 * #%L
 * fuse-dv-unipop
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unipop.process.Profiler;
import org.unipop.query.aggregation.ReduceQuery;

import static org.opensearch.graph.dispatcher.logging.LogMessage.Level.trace;

public class LoggingReduceController implements ReduceQuery.SearchController {
    //region Constructors
    public LoggingReduceController(
            ReduceQuery.SearchController searchController,
            MetricRegistry metricRegistry) {
        this.logger = LoggerFactory.getLogger(searchController.getClass());
        this.searchController = searchController;
        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region ReduceQuery.SearchController Implementation
    @Override
    public long count(ReduceQuery reduceQuery) {
        return new LoggingSyncMethodDecorator<Long>(this.logger, this.metricRegistry, count,
                StepDescriptorLogWriter.of("Reduce["+reduceQuery.getStepDescriptor().getDescription().orElse("?")+"]"),trace)
                .decorate(() -> this.searchController.count(reduceQuery), new Passthrough<>((ex) -> 0L));
    }

    @Override
    public long max(ReduceQuery reduceQuery) {
        return new LoggingSyncMethodDecorator<Long>(this.logger, this.metricRegistry, count, trace)
                .decorate(() -> this.searchController.max(reduceQuery), new Passthrough<>((ex) -> 0L));
    }

    @Override
    public long min(ReduceQuery reduceQuery) {
        return new LoggingSyncMethodDecorator<Long>(this.logger, this.metricRegistry, count, trace)
                .decorate(() -> this.searchController.min(reduceQuery), new Passthrough<>((ex) -> 0L));
    }

    @Override
    public long avg(ReduceQuery reduceQuery) {
        return new LoggingSyncMethodDecorator<Long>(this.logger, this.metricRegistry, count, trace)
                .decorate(() -> this.searchController.avg(reduceQuery), new Passthrough<>((ex) -> 0L));
    }

    @Override
    public Profiler getProfiler() {
        return this.searchController.getProfiler();
    }

    @Override
    public void setProfiler(Profiler profiler) {
        this.searchController.setProfiler(profiler);
    }

    //endregion

    //region Fields
    private Logger logger;
    private MetricRegistry metricRegistry;
    private ReduceQuery.SearchController searchController;

    private static MethodName.MDCWriter count = MethodName.of("count");
    //endregion
}
