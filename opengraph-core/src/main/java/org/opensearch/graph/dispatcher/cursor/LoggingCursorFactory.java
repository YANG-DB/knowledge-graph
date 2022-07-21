package org.opensearch.graph.dispatcher.cursor;




import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;

public class LoggingCursorFactory implements CursorFactory {
    public static final String cursorFactoryParameter = "LoggingCursorFactory.@cursorFactory";
    public static final String loggerParameter = "LoggingCursorFactory.@logger";

    //region Constructors
    @Inject
    public LoggingCursorFactory(
            MetricRegistry metricRegistry,
            @Named(cursorFactoryParameter) CursorFactory cursorFactory,
            @Named(loggerParameter) Logger logger) {
        this.metricRegistry = metricRegistry;
        this.cursorFactory = cursorFactory;
        this.logger = logger;
    }
    //endregion

    //region LoggingFactory Implementation
    @Override
    public Cursor createCursor(Context context) {
        return new LoggingCursor(this.cursorFactory.createCursor(context), this.logger, metricRegistry);
    }
    //endregion

    private MetricRegistry metricRegistry;
    //region Fields
    private CursorFactory cursorFactory;
    private Logger logger;
    //endregion
}
