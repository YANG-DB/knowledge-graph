package org.opensearch.graph.services.appRegistrars;




import org.opensearch.graph.dispatcher.urlSupplier.AppUrlSupplier;
import org.opensearch.graph.services.controllers.InternalsController;
import org.jooby.Jooby;
import org.jooby.Results;

public class InternalsControllerRegistrar extends AppControllerRegistrarBase<InternalsController> {
    //region Constructors
    public InternalsControllerRegistrar() {
        super(InternalsController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** get the health status of the service */
        app.get("/fuse/internal/statisticsProvider/name",
                req -> Results.with(this.getController(app).getStatisticsProviderName()));
        app.get("/fuse/internal/version",
                req -> Results.with(this.getController(app).getVersion()));
        app.get("/fuse/internal/config",
                req -> Results.with(this.getController(app).getConfig()));
        app.get("/fuse/internal/snowflakeId",
                req -> Results.with(this.getController(app).getSnowflakeId()));
        app.get("/fuse/internal/cursorBindings",
                req -> Results.with(this.getController(app).getCursorBindings()));
        app.get("/fuse/internal/statisticsProvider/setup",
                req -> Results.with(this.getController(app).getStatisticsProviderSetup()));
        app.put("/fuse/internal/statisticsProvider/refresh",
                req -> Results.with(this.getController(app).refreshStatisticsProviderSetup()));
    }
    //endregion
}
