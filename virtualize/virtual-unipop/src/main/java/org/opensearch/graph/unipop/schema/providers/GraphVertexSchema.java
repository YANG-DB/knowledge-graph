package org.opensearch.graph.unipop.schema.providers;

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





import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.schema.BaseTypeElement.Type;
import org.opensearch.graph.unipop.schema.providers.indexPartitions.IndexPartitions;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface GraphVertexSchema extends GraphElementSchema {
    default Class getSchemaElementType() {
        return Vertex.class;
    }

    List<GraphVertexSchema> getNestedSchemas();
    void setNestedSchemas(List<GraphVertexSchema> schemas);


    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    class Impl extends GraphElementSchema.Impl implements GraphVertexSchema {
        //region Constructors
        public Impl(Type label ) {
            super(label);
        }

        public Impl(Type label , GraphElementRouting routing) {
            super(label, routing);
        }

        public Impl(Type label , IndexPartitions indexPartitions) {
            super(label, indexPartitions);
        }

        public Impl(Type label , GraphElementRouting routing, IndexPartitions indexPartitions) {
            super(label, routing, indexPartitions);
        }

        public Impl(Type label , IndexPartitions indexPartitions, Iterable<GraphElementPropertySchema> properties) {
            super(label, indexPartitions, properties);
        }

        public Impl(Type label ,
                    GraphElementConstraint constraint,
                    Optional<GraphElementRouting> routing,
                    Optional<IndexPartitions> indexPartitions,
                    Iterable<GraphElementPropertySchema> properties) {
            super(label, constraint, routing, indexPartitions, properties);
        }
        //endregion


        public void setNestedSchemas(List<GraphVertexSchema> nestedSchemas) {
            this.nestedSchemas = nestedSchemas;
       }

        @Override
        public List<GraphVertexSchema> getNestedSchemas() {
            return nestedSchemas;
        }

        private List<GraphVertexSchema> nestedSchemas = new ArrayList<>();
    }
}
