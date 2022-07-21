package org.opensearch.graph.unipop.controller.common.logging;



import com.codahale.metrics.MetricRegistry;
import org.opensearch.graph.dispatcher.decorators.MethodDecorator.ResultHandler.Passthrough;
import org.opensearch.graph.dispatcher.logging.*;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unipop.process.Profiler;
import org.unipop.query.search.SearchQuery;

import java.util.Collections;
import java.util.Iterator;

import static org.opensearch.graph.dispatcher.logging.LogMessage.Level.trace;

public class LoggingSearchController implements SearchQuery.SearchController {
    //region Constructors
    public LoggingSearchController(
            SearchQuery.SearchController searchController,
            MetricRegistry metricRegistry) {
        this.logger = LoggerFactory.getLogger(searchController.getClass());
        this.metricRegistry = metricRegistry;
        this.searchController = searchController;
    }
    //endregion

    //region SearchQuery.SearchController Implementation
    @Override
    public <E extends Element> Iterator<E> search(SearchQuery<E> searchQuery) {
        return new LoggingSyncMethodDecorator<Iterator<E>>(this.logger, this.metricRegistry, search, trace)
                .decorate(() -> this.searchController.search(searchQuery), new Passthrough<>((ex) -> Collections.emptyIterator()));
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
    private SearchQuery.SearchController searchController;

    private static MethodName.MDCWriter search = MethodName.of("search");
    //endregion
}
