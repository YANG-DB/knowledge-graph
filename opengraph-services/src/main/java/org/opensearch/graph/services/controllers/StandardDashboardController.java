package org.opensearch.graph.services.controllers;

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




import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.opensearch.graph.dispatcher.cursor.CompositeCursorFactory;
import org.opensearch.graph.dispatcher.driver.DashboardDriver;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.model.transport.ContentResponse.Builder;
import org.opensearch.graph.services.suppliers.RequestIdSupplier;
import org.opensearch.graph.services.suppliers.CachedRequestIdSupplier;

import java.util.Optional;
import java.util.Set;

import static org.opensearch.graph.model.transport.Status.*;

/**
 * Created by lior.perry on 19/02/2017.
 */
public class StandardDashboardController implements DashboardController<StandardDashboardController,DashboardDriver> {
    //region Constructors
    @Inject
    public StandardDashboardController(
            DashboardDriver driver,
            @Named(CachedRequestIdSupplier.RequestIdSupplierParameter) RequestIdSupplier requestIdSupplier,
            Set<CompositeCursorFactory.Binding> cursorBindings) {
        this.driver = driver;
        this.requestIdSupplier = requestIdSupplier;
        this.cursorBindings = cursorBindings;
    }
    //endregion

    @Override
    public ContentResponse<ObjectNode> graphElementCount(String ontology) {
        return Builder.<ObjectNode>builder(ACCEPTED, NOT_FOUND)
                .data(Optional.of(driver().graphElementCount(ontology)))
                .compose();
    }

    @Override
    public ContentResponse<ObjectNode> graphElementCreatedOverTime(String ontology) {
        return Builder.<ObjectNode>builder(ACCEPTED, NOT_FOUND)
                .data(Optional.of(driver().graphElementCreated(ontology)))
                .compose();
    }

    @Override
    public ContentResponse<ObjectNode> cursorCount() {
        return Builder.<ObjectNode>builder(ACCEPTED, NOT_FOUND)
                .data(Optional.of(driver().cursorCount()))
                .compose();
    }

    protected DashboardDriver driver() {
        return driver;
    }

    //endregion

    @Override
    public StandardDashboardController driver(DashboardDriver driver) {
        this.driver = driver;
        return this;
    }

    //region Fields
    private DashboardDriver driver;
    private RequestIdSupplier requestIdSupplier;
    private Set<CompositeCursorFactory.Binding> cursorBindings;

    //endregion
}
