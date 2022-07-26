package org.opensearch.graph.services.controllers;




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
