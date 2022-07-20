package org.opensearch.graph.dispatcher.descriptors;

/*-
 * #%L
 * opengraph-core
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



import org.opensearch.graph.dispatcher.resource.CursorResource;
import org.opensearch.graph.model.descriptors.Descriptor;

/**
 * Created by roman.margolis on 29/11/2017.
 */
public class CursorResourceDescriptor implements Descriptor<CursorResource> {
    //region Descriptor Implementation
    @Override
    public String describe(CursorResource item) {
        return String.format("Cursor{id: %s, type: %s}", item.getCursorId(), item.getCursorRequest());
    }
    //endregion
}
