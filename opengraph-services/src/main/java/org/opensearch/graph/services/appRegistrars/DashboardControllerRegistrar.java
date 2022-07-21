package org.opensearch.graph.services.appRegistrars;


import org.opensearch.graph.dispatcher.urlSupplier.AppUrlSupplier;
import org.opensearch.graph.services.controllers.DashboardController;
import org.jooby.Jooby;
import org.jooby.Results;

public class DashboardControllerRegistrar extends AppControllerRegistrarBase<DashboardController> {
    //region Constructors
    public DashboardControllerRegistrar() {
        super(DashboardController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        app.get("/fuse/dashboard/entities/:ontology",
                req -> Results.with(this.getController(app).graphElementCount(req.param("ontology").value())));
        app.get("/fuse/dashboard/created/:ontology",
                req -> Results.with(this.getController(app).graphElementCreatedOverTime(req.param("ontology").value())));
        app.get("/fuse/dashboard/count",
                req -> Results.with(this.getController(app).cursorCount()));
    }
    //endregion
}
