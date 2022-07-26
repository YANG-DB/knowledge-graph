package org.opensearch.graph.services.appRegistrars;




import com.google.inject.TypeLiteral;
import org.opensearch.graph.dispatcher.urlSupplier.AppUrlSupplier;
import org.opensearch.graph.model.Range;
import org.opensearch.graph.services.controllers.IdGeneratorController;
import org.jooby.Jooby;
import org.jooby.Results;

import java.util.Arrays;

public class IdGeneratorControllerRegistrar extends AppControllerRegistrarBase<IdGeneratorController<Range>> {
    //region Constructors
    public IdGeneratorControllerRegistrar() {
        super(new TypeLiteral<IdGeneratorController<Range>>(){});
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        app.get("/fuse/idgen/init",
                req -> Results.with(this.getController(app).init(Arrays.asList(req.param("names").value().split(",")))));
        app.get("/fuse/idgen",
                req -> this.getController(app).getNext(req.param("id").value(), req.param("numIds").intValue()));
    }

    //endregion
}
