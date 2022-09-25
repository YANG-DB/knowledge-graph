package org.opensearch.graph.dispatcher.cursor;

/*-
 * #%L
 * opengraph-core
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
import org.opensearch.graph.dispatcher.logging.*;
import org.opensearch.graph.model.resourceInfo.GraphError;
import org.opensearch.graph.model.results.QueryResultBase;
import org.slf4j.Logger;

public class LoggingCursor implements Cursor {

    public static final String CURSOR = "cursor";

    public static final String CURSOR_COUNT = CURSOR+".count";

    //region Constructors
    public LoggingCursor(Cursor cursor, Logger logger, MetricRegistry metricRegistry) {
        this.cursor = cursor;
        this.logger = logger;
        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region Cursor Implementation
    @Override
    public QueryResultBase getNextResults(int numResults) {
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.logger, LogMessage.Level.trace, "start getNextResults", sequence, LogType.of(LogType.start), getNextResults, ElapsedFrom.now()).log();
            metricRegistry.counter(CURSOR_COUNT).inc(1);
            return this.cursor.getNextResults(numResults);
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.logger, LogMessage.Level.error, "failed getNextResults", sequence, LogType.of(LogType.failure), getNextResults, ElapsedFrom.now())
                    .with(ex).log();
            throw new GraphError.GraphErrorException(new GraphError("Cursor Error",ex));
        } finally {
            metricRegistry.counter(CURSOR_COUNT).dec(1);
            if (!thrownException) {
                new LogMessage.Impl(this.logger, LogMessage.Level.trace, "finish getNextResults", sequence, LogType.of(LogType.success), getNextResults, ElapsedFrom.now()).log();
            }
        }
    }

    @Override
    public Object getContext() {
        return cursor.getContext();
    }

    @Override
    public int getActiveScrolls() {
        return cursor.getActiveScrolls();
    }


    @Override
    public boolean clearScrolls() {
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.logger, LogMessage.Level.trace, "start cleanResources", sequence, LogType.of(LogType.start), cleanResources, ElapsedFrom.now()).log();
            return this.cursor.clearScrolls();
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.logger, LogMessage.Level.error, "failed cleanResources", sequence, LogType.of(LogType.failure), cleanResources, ElapsedFrom.now())
                    .with(ex).log();
            throw new GraphError.GraphErrorException(new GraphError("Cursor Error",ex));
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.logger, LogMessage.Level.trace, "finish cleanResources", sequence, LogType.of(LogType.success), cleanResources, ElapsedFrom.now()).log();
            }
        }
    }
    //endregion

    //region Fields
    private Cursor cursor;
    private Logger logger;
    private MetricRegistry metricRegistry;

    private static MethodName.MDCWriter getNextResults = MethodName.of("getNextResults");
    private static MethodName.MDCWriter cleanResources = MethodName.of("cleanResources");
    private static LogMessage.MDCWriter sequence = Sequence.incr();

    //endregion
}
