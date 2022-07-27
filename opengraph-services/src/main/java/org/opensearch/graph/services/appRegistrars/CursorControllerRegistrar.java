package org.opensearch.graph.services.appRegistrars;

/*-
 * #%L
 * opengraph-services
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */




import org.opensearch.graph.dispatcher.urlSupplier.AppUrlSupplier;
import org.opensearch.graph.logging.Route;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.model.transport.ExecutionScope;
import org.opensearch.graph.model.transport.cursor.CreateCursorRequest;
import org.opensearch.graph.services.controllers.CursorController;
import org.jooby.Jooby;
import org.jooby.Results;

public class CursorControllerRegistrar extends AppControllerRegistrarBase<CursorController> {
    //region Constructors
    public CursorControllerRegistrar() {
        super(CursorController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** get the query cursor store info */
        app.get(appUrlSupplier.cursorStoreUrl(":queryId"),
                req -> {
                    Route.of("getCursorStore").write();

                    ContentResponse response = this.getController(app).getInfo(req.param("queryId").value());
                    return Results.with(response, response.status().getStatus());
                });

        /** create a query cursor */
        app.post(appUrlSupplier.cursorStoreUrl(":queryId"),
                req -> {
                    Route.of("postCursor").write();
                    CreateCursorRequest cursorRequest = req.body(CreateCursorRequest.class);
                    req.set(ExecutionScope.class, new ExecutionScope(Math.max(cursorRequest.getMaxExecutionTime(),1000 * 60 * 10)));
                    ContentResponse response = this.getController(app).create(req.param("queryId").value(), cursorRequest);

                    return Results.with(response, response.status().getStatus());
                });

        /** get the cursor resource info */
        app.get(appUrlSupplier.resourceUrl(":queryId", ":cursorId"),
                req -> {
                    Route.of("getCursor").write();

                    ContentResponse response = this.getController(app).getInfo(req.param("queryId").value(), req.param("cursorId").value());
                    return Results.with(response, response.status().getStatus());
                });

        app.delete(appUrlSupplier.resourceUrl(":queryId", ":cursorId"),
                req -> {
                    Route.of("deleteCursor").write();

                    ContentResponse response = this.getController(app).delete(req.param("queryId").value(), req.param("cursorId").value());
                    return Results.with(response, response.status().getStatus());
                });
    }
    //endregion
}
