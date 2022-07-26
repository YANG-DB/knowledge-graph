package org.opensearch.graph.services.appRegistrars;




import org.opensearch.graph.dispatcher.urlSupplier.AppUrlSupplier;
import org.jooby.Jooby;

public class HealthAppRegistrar implements AppRegistrar {
    //region AppRegistrar Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** get the health status of the service */
        app.get("/fuse/health",() -> "Alive And Well...");
    }
    //endregion
}
