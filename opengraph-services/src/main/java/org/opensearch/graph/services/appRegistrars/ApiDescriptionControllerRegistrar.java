package org.opensearch.graph.services.appRegistrars;




import org.opensearch.graph.dispatcher.urlSupplier.AppUrlSupplier;
import org.opensearch.graph.services.controllers.ApiDescriptionController;
import org.jooby.Jooby;

public class ApiDescriptionControllerRegistrar extends AppControllerRegistrarBase<ApiDescriptionController> {
    //region Constructors
    public ApiDescriptionControllerRegistrar() {
        super(ApiDescriptionController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        app.get("/fuse",() -> this.getController(app).getInfo());
    }
    //endregion
}
