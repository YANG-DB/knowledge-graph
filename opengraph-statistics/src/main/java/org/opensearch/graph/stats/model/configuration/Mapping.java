package org.opensearch.graph.stats.model.configuration;

/*-
 * #%L
 * opengraph-statistics
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by benishue on 30-Apr-17.
 */
public class Mapping {

    //region Ctrs
    public Mapping() {
        //needed for Jackson
    }

    public Mapping(List<String> indices, List<String> types) {
        this.indices = indices;
        this.types = types;
    }
    //endregion

    //region Getters & Setters
    public List<String> getIndices() {
        return indices;
    }

    public void setIndices(List<String> indices) {
        this.indices = indices;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }
    //endregion

    //region Fields
    private List<String> indices;
    private List<String> types;
    //endregion

    //region Builder
    public static final class Builder {
        private List<String> indices;
        private List<String> types;

        private Builder() {
            this.indices = new ArrayList<>();
            this.types = new ArrayList<>();
        }

        public static Builder get() {
            return new Builder();
        }

        public Builder withIndices(List<String> indices) {
            this.indices = indices;
            return this;
        }

        public Builder withIndex(String index) {
            this.indices.add(index);
            return this;
        }

        public Builder withTypes(List<String> types) {
            this.types = types;
            return this;
        }

        public Builder withType(String type) {
            this.types.add(type);
            return this;
        }

        public Mapping build() {
            Mapping mapping = new Mapping();
            mapping.setIndices(indices);
            mapping.setTypes(types);
            return mapping;
        }
    }
    //endregion

}
