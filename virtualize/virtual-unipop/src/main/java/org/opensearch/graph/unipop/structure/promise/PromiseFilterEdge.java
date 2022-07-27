package org.opensearch.graph.unipop.structure.promise;

/*-
 * #%L
 * virtual-unipop
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





import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.unipop.controller.utils.map.MapBuilder;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.structure.UniEdge;
import org.unipop.structure.UniGraph;

public class PromiseFilterEdge extends UniEdge {

    //region Constructor
    public PromiseFilterEdge(Vertex v,  UniGraph graph) {
        super(new MapBuilder<String, Object>()
                        .put(T.id.getAccessor(), GlobalConstants.Labels.PROMISE_FILTER + v.id()).get(),
                v,
                v,
                graph);
    }
    //endregion

    //region Override Methods
    @Override
    protected String getDefaultLabel() {
        return GlobalConstants.Labels.PROMISE_FILTER;
    }

    @Override
    public String toString() {
        return String.format(PRINT_FORMAT, outVertex.id(), id, inVertex.id());
    }
    //endregion

    //region Static
    private static String PRINT_FORMAT = "%s --(%s)--> %s";
    //endregion
}
