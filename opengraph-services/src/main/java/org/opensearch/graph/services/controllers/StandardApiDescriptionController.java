package org.opensearch.graph.services.controllers;


import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.urlSupplier.AppUrlSupplier;
import org.opensearch.graph.model.resourceInfo.FuseResourceInfo;
import org.opensearch.graph.model.transport.ContentResponse;

import java.util.Optional;

import static org.opensearch.graph.model.transport.Status.*;

/**
 * Created by Roman on 11/06/2017.
 */
public class StandardApiDescriptionController implements ApiDescriptionController {
    //region Constructors
    @Inject
    public StandardApiDescriptionController(AppUrlSupplier urlSupplier) {
        this.urlSupplier = urlSupplier;
    }
    //endregion

    //region ApiDescriptionController Implementation
    @Override
    public ContentResponse<FuseResourceInfo> getInfo() {
        return ContentResponse.Builder.<FuseResourceInfo>builder(OK, NOT_FOUND)
                .data(Optional.of(new FuseResourceInfo(
                        "/fuse",
                        "/fuse/internal",
                        "/fuse/health",
                        this.urlSupplier.queryStoreUrl(),
                        "/fuse/search", this.urlSupplier.catalogStoreUrl())))
                .compose();
    }
    //endregion

    //region Fields
    protected final AppUrlSupplier urlSupplier;
    //endregion
}
