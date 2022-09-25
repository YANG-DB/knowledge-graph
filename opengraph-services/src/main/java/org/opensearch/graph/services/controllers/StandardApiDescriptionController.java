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




import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.urlSupplier.AppUrlSupplier;
import org.opensearch.graph.model.resourceInfo.GraphResourceInfo;
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
    public ContentResponse<GraphResourceInfo> getInfo() {
        return ContentResponse.Builder.<GraphResourceInfo>builder(OK, NOT_FOUND)
                .data(Optional.of(new GraphResourceInfo(
                        "/opengraph",
                        "/opengraph/internal",
                        "/opengraph/health",
                        this.urlSupplier.queryStoreUrl(),
                        "/opengraph/search", this.urlSupplier.catalogStoreUrl())))
                .compose();
    }
    //endregion

    //region Fields
    protected final AppUrlSupplier urlSupplier;
    //endregion
}
