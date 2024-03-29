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
import org.opensearch.graph.model.execution.plan.descriptors.OntologyDescriptor;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.resourceInfo.GraphError;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.model.transport.Status;
import org.opensearch.graph.services.rendering.SVGGraphRenderer;
import org.opensearch.graph.services.controllers.CatalogController;
import org.jooby.Jooby;
import org.jooby.Request;
import org.jooby.Response;
import org.jooby.Results;

import java.io.File;
import java.util.List;

import static org.jooby.Status.NOT_FOUND;
import static org.jooby.Status.OK;

public class CatalogControllerRegistrar extends AppControllerRegistrarBase<CatalogController> {
    //region Constructors
    public CatalogControllerRegistrar() {
        super(CatalogController.class);
    }
    //endregion

    //region AppControllerRegistrarBase Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        /** create new ontology*/
        app.post("/opengraph/catalog/ontology"
                ,req -> {
                    Route.of("postPage").write();
                    Ontology ontology = req.body(Ontology.class);
                    req.set(Ontology.class, ontology);
                    ContentResponse<Ontology> response = this.getController(app).addOntology(ontology);
                    return Results.with(response, response.status().getStatus());
                });

        /** get available ontologies*/
        app.get("/opengraph/catalog/ontology"
                ,req -> {
                    ContentResponse<List<Ontology>> response = this.getController(app).getOntologies();
                    return Results.with(response, response.status().getStatus());
                });

        /** get the ontology by id */
        app.get("/opengraph/catalog/ontology/:id"
                ,req -> {
                    ContentResponse response = this.getController(app).getOntology(req.param("id").value());
                    return Results.with(response, response.status().getStatus());
                });

        /** get the ontology by id */
        app.get("/opengraph/catalog/ontology/:id/visualize",
                     (req,resp) -> API.dataView(app,req,resp,this));

        /** get available schemas **/
        app.get("/opengraph/catalog/schema"
                ,req -> {
                    ContentResponse<List<String>> response = this.getController(app).getSchemas();
                    return Results.with(response, response.status().getStatus());
                });

        app.get("/opengraph/catalog/schema/:id",
                req -> {
                    ContentResponse response = this.getController(app).getSchema(req.param("id").value());
                    return Results.with(response, response.status().getStatus());
                });
    }

    public static class API {
        /**
         * print an SVG representation of the data
         * @param app
         * @param req
         * @param resp
         * @param controller
         * @return
         * @throws Throwable
         */
        public static Object dataView(Jooby app, Request req, Response resp, CatalogControllerRegistrar controller) throws Throwable {
            Route.of("getCatalogOntologyVisualization").write();
            ContentResponse<Ontology> ontology = controller.getController(app).getOntology(req.param("id").value());
            String dotGraph = OntologyDescriptor.printGraph(ontology.getData());
            File file = SVGGraphRenderer.render(ontology.getData().getOnt(), dotGraph);
            ContentResponse<File> compose = ContentResponse.Builder.<File>builder(Status.OK, Status.NOT_FOUND)
                    .data(file)
                    .compose();
            return RegistrarsUtils.withImg(req,resp,compose);
        }


    }

    //endregion
}
