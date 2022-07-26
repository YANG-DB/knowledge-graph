package org.opensearch.graph.dragons.services;



import org.opensearch.graph.dispatcher.urlSupplier.AppUrlSupplier;
import org.opensearch.graph.logging.Route;
import org.opensearch.graph.model.resourceInfo.QueryResourceInfo;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.model.transport.CreateJsonQueryRequest;
import org.opensearch.graph.model.transport.ExecutionScope;
import org.opensearch.graph.model.transport.PlanTraceOptions;
import org.opensearch.graph.services.appRegistrars.AppControllerRegistrarBase;
import org.opensearch.graph.services.appRegistrars.RegistrarsUtils;
import org.opensearch.graph.services.controllers.QueryController;
import org.jooby.*;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Map;

import static org.opensearch.graph.services.appRegistrars.QueryControllerRegistrar.API.runCypher;
import static org.opensearch.graph.services.appRegistrars.QueryControllerRegistrar.API.runV1;


public class DragonsExtensionRegistrar extends AppControllerRegistrarBase<DragonsExtensionQueryController> {

    //region Constructors
    public DragonsExtensionRegistrar() {
        super(DragonsExtensionQueryController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        app.get("ext", () -> Results.redirect("/public/assets/swagger/swagger-ext.json"));

        /** create a clause query */
        app.post(appUrlSupplier.queryStoreUrl() + "/clause",req -> postClause(app,req,this.getController(app)));
        /** run a clause query */
        app.post(appUrlSupplier.queryStoreUrl() + "/clause/run",( req,res) -> runClause(app,req,res,this.getController(app)));
        /** run a cypher query */
        app.get(appUrlSupplier.queryStoreUrl() + "/cypher/logical/run",( req,res) -> runCypher(app,req, res,this.getController(app)));
        /** run a v1 query */
        app.post(appUrlSupplier.queryStoreUrl() + "/query/v1/logical/run",( req,res)-> runV1(app,req,res, this.getController(app)));

    }

    public static Result postClause(Jooby app, final Request req, QueryController controller) throws Exception {
        Route.of("postClause").write();

        Map<String,Object> createQueryRequest = req.body(Map.class);
        String query = new JSONObject((Map) createQueryRequest.get("query")).toString();
        CreateJsonQueryRequest request = new CreateJsonQueryRequest(
                createQueryRequest.get("id").toString(),
                createQueryRequest.get("name").toString(),
                createQueryRequest.get("queryType").toString(),
                query,
                createQueryRequest.get("ontology").toString());

        req.set(CreateJsonQueryRequest.class, request);
        req.set(PlanTraceOptions.class, request.getPlanTraceOptions());
        final long maxExecTime = request.getCreateCursorRequest() != null
                ? request.getCreateCursorRequest().getMaxExecutionTime() : 0;
        req.set(ExecutionScope.class, new ExecutionScope(Math.max(maxExecTime, 1000 * 60 * 10)));

        ContentResponse<QueryResourceInfo> response = request.getCreateCursorRequest() == null ?
                controller.create(request) :
                controller.createAndFetch(request);

        return Results.with(response, response.status().getStatus());

    }


    public static void runClause(Jooby app, final Request req, final Response resp, QueryController controller) throws Throwable {
        Route.of("runClause").write();

        Map<String,Object> createQueryRequest = req.body(Map.class);
        String query = new JSONObject(Collections.singletonMap("clause",createQueryRequest.get("clause"))).toString();
        String ontology = req.param("ontology").value();
        req.set(ExecutionScope.class, new ExecutionScope(1000 * 60 * 10));

        ContentResponse<Object> response = controller.runCypher(query,ontology);

        RegistrarsUtils.with(req,resp, response);
    }
    //endregion
}
