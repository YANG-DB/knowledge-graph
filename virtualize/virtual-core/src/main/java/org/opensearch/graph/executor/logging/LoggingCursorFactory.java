package org.opensearch.graph.executor.logging;


import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.opensearch.graph.dispatcher.cursor.Cursor;
import org.opensearch.graph.dispatcher.cursor.CursorFactory;
import org.opensearch.graph.dispatcher.cursor.LoggingCursor;
import org.opensearch.graph.executor.cursor.TraversalCursorContext;
import org.slf4j.Logger;

public class LoggingCursorFactory implements CursorFactory {
    public static final String cursorFactoryParameter = "LoggingCursorFactory.@cursorFactory";
    public static final String cursorLoggerParameter = "LoggingCursorFactory.@cursorLogger";
    public static final String traversalLoggerParameter = "LoggingCursorFactory.@traversalLogger";

    //region Constructors
    @Inject
    public LoggingCursorFactory(
            MetricRegistry metricRegistry,
            @Named(cursorFactoryParameter) CursorFactory cursorFactory,
            @Named(cursorLoggerParameter) Logger cursorLogger,
            @Named(traversalLoggerParameter) Logger traversalLogger) {
        this.metricRegistry = metricRegistry;
        this.cursorFactory = cursorFactory;
        this.cursorLogger = cursorLogger;
        this.traversalLogger = traversalLogger;
    }
    //endregion

    //region LoggingFactory Implementation
    @Override
    public Cursor createCursor(Context context) {
        TraversalCursorContext traversalCursorContext = (TraversalCursorContext)context;
        TraversalCursorContext loggingTraversalCursorContext = traversalCursorContext.clone();
        loggingTraversalCursorContext.setTraversal(new LoggingTraversal<>(traversalCursorContext.getTraversal(), this.traversalLogger));
        return new LoggingCursor(this.cursorFactory.createCursor(loggingTraversalCursorContext), this.cursorLogger, metricRegistry);
    }
    //endregion

    private MetricRegistry metricRegistry;
    //region Fields
    private CursorFactory cursorFactory;
    private Logger cursorLogger;
    private Logger traversalLogger;
    //endregion
}
