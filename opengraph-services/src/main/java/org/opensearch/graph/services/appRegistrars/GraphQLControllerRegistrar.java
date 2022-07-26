package org.opensearch.graph.services.appRegistrars;




import org.opensearch.graph.dispatcher.urlSupplier.AppUrlSupplier;
import org.opensearch.graph.logging.Route;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.services.controllers.languages.graphql.StandardGraphQLController;
import org.jooby.Jooby;
import org.jooby.Results;

public class GraphQLControllerRegistrar extends AppControllerRegistrarBase<StandardGraphQLController> {
    //region Constructors
    public GraphQLControllerRegistrar() {
        super(StandardGraphQLController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** create new ontology*/
        app.post("/fuse/graphql/ontology/:id"
                ,req -> {
                    Route.of("translateGraphQLSchema").write();
                    String graphQLSchemas = req.body(String.class);
                    req.set(String.class, graphQLSchemas);
                    ContentResponse<Ontology> response = this.getController(app).translate(req.param("id").value(), graphQLSchemas);
                    return Results.with(response, response.status().getStatus());
                });
    }
    //endregion
}
