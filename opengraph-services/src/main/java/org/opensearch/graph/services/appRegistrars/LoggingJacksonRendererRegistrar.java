package org.opensearch.graph.services.appRegistrars;




import com.codahale.metrics.MetricRegistry;
import org.opensearch.graph.dispatcher.urlSupplier.AppUrlSupplier;
import org.opensearch.graph.services.modules.LoggingJacksonModule;
import org.jooby.Jooby;
import org.jooby.json.Jackson;

public class LoggingJacksonRendererRegistrar implements AppRegistrar {
    //region Constructors
    public LoggingJacksonRendererRegistrar(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region AppRegistrar Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        app.use(new LoggingJacksonModule(this.metricRegistry));
        app.use(new Jackson());
    }
    //endregion

    //region Fields
    protected MetricRegistry metricRegistry;
    //endregion
}
