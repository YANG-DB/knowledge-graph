package org.opensearch.graph.unipop.controller.common.logging;



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
