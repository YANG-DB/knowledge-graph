package org.opensearch.graph.unipop.controller.utils.idProvider;

/*-
 * #%L
 * fuse-dv-unipop
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

import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Map;

/**
 * Created by roman.margolis on 16/11/2017.
 */
public class SimpleEdgeIdProvider implements EdgeIdProvider<String> {
    //region EdgeIdProvider Implementation
    @Override
    public String get(String edgeLabel, Vertex outV, Vertex inV, Map<String, Object> properties) {
        return String.format("%s.%s.%s", edgeLabel, outV.id(), inV.id());
    }
    //endregion
}
