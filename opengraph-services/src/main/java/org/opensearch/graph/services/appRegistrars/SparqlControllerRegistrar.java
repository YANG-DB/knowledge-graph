package org.opensearch.graph.services.appRegistrars;




import org.opensearch.graph.dispatcher.urlSupplier.AppUrlSupplier;
import org.opensearch.graph.logging.Route;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.services.controllers.languages.sparql.StandardSparqlController;
import org.jooby.Jooby;
import org.jooby.Results;

public class SparqlControllerRegistrar extends AppControllerRegistrarBase<StandardSparqlController> {
    //region Constructors
    public SparqlControllerRegistrar() {
        super(StandardSparqlController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** create new ontology*/
        app.post("/fuse/sparql/ontology/:id"
                ,req -> {
                    Route.of("translateOwlSchema").write();
                    String owlSchmea = req.body(String.class);
                    req.set(String.class, owlSchmea);
                    ContentResponse<Ontology> response = this.getController(app).translate(req.param("id").value(), owlSchmea);
                    return Results.with(response, response.status().getStatus());
                });
    }
    //endregion
}
