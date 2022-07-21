package org.opensearch.graph.services.controllers;


import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.driver.IdGeneratorDriver;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.client.Client;

import java.util.List;
import java.util.Optional;

import static org.opensearch.graph.model.transport.Status.*;

/**
 * Created by roman.margolis on 20/03/2018.
 */
public class StandardIdGeneratorController<TId> implements IdGeneratorController<TId> {
    //region Constructors
    @Inject
    public StandardIdGeneratorController(IdGeneratorDriver<TId> driver ) {
        this.driver = driver;
    }
    //endregion

    //region IdGenerator Implementation
    @Override
    public ContentResponse<TId> getNext(String genName, int numIds) {
        return ContentResponse.Builder.<TId>builder(CREATED, INTERNAL_SERVER_ERROR )
                .data(Optional.of(this.driver.getNext(genName, numIds)))
                .compose();
    }

    @Override
    public ContentResponse<Boolean> init(List<String> names) {
        return ContentResponse.Builder.<Boolean>builder(CREATED, INTERNAL_SERVER_ERROR )
                .data(Optional.of(this.driver.init(names)))
                .compose();
    }
    //endregion

    //region Fields
    private IdGeneratorDriver<TId> driver;
    //endregion
}
