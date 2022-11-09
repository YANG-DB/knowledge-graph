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

public interface GraphRedundantPropertySchema extends GraphElementPropertySchema{
    String getPropertyRedundantName();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    class Impl extends GraphElementPropertySchema.Impl implements GraphRedundantPropertySchema {
        //region Constructors
        public Impl(String name, String redundantName, String type) {
            super(name, type);
            this.propertyRedundantName = redundantName;
        }

        public Impl(String name,String pType, String redundantName, String type, Iterable<IndexingSchema> indexingSchemes) {
            super(name, pType, type, indexingSchemes);
            this.propertyRedundantName = redundantName;
        }
        //endregion

        //region GraphElementPropertySchema Implementation
        @Override
        public String getPropertyRedundantName() {
            return this.propertyRedundantName;
        }
        //endregion

        //region Fields
        private String propertyRedundantName;
        //endregion
    }
}
