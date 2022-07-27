package org.opensearch.graph.core.driver;

/*-
 * #%L
 * virtual-core
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
import org.opensearch.graph.client.export.GraphWriter;
import org.opensearch.graph.client.export.GraphWriterStrategy;
import org.opensearch.graph.dispatcher.driver.PageDriverBase;
import org.opensearch.graph.dispatcher.resource.CursorResource;
import org.opensearch.graph.dispatcher.resource.PageResource;
import org.opensearch.graph.dispatcher.resource.QueryResource;
import org.opensearch.graph.dispatcher.resource.store.ResourceStore;
import org.opensearch.graph.dispatcher.urlSupplier.AppUrlSupplier;
import org.opensearch.graph.model.results.QueryResultBase;
import org.opensearch.graph.model.transport.cursor.CreateGraphCursorRequest;
import org.opensearch.graph.model.transport.cursor.LogicalGraphCursorRequest;

import java.util.Map;

public class StandardPageDriver extends PageDriverBase {
    //region Constructors
    @Inject
    public StandardPageDriver(ResourceStore resourceStore, AppUrlSupplier urlSupplier, GraphWriterStrategy strategy) {
        super(resourceStore, urlSupplier, strategy);
    }
    //endregion

    //region PageDriverBase Implementation
    @Override
    protected PageResource<QueryResultBase> createResource(QueryResource queryResource, CursorResource cursorResource, String pageId, int pageSize) {
        PageResource<QueryResultBase> pageResource = new PageResource<>(pageId, pageSize);
        //drain results from storage
        QueryResultBase results = cursorResource.getCursor().getNextResults(pageSize);
        //populate the page resource
        return pageResource
                .withResults(results)
                .withActualSize(results.getSize())
                .available();
    }
    //endregion
}
