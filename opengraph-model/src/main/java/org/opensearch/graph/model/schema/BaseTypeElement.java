package org.opensearch.graph.model.schema;

/*-
 * #%L
 * opengraph-model
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





import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface BaseTypeElement<T> {
    List<T> getNested();

    Props getProps();

    String getMapping();

    String getPartition();

     Type getType();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    class Type {
        public static final Type Unknown = new Type("",Optional.empty(),true);
        private String name;
        private Optional<String> field = Optional.empty();
        private boolean implicit;

        private Type() {}
        public Type(String name) {
            this(name,Optional.empty(),false);
        }
        public Type(String name, Optional<String> field, boolean implicit) {
            this.name = name;
            this.field = field;
            this.implicit = implicit;
        }

        @JsonIgnore
        public static Type of(String name) {
            return new Type(name,Optional.empty(),false);
        }

        @JsonProperty("name")
        public String getName() {
            return name;
        }

        @JsonProperty("field")
        public Optional<String> getField() {
            return field;
        }

        @JsonProperty("implicit")
        public boolean isImplicit() {
            return implicit;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Type type = (Type) o;
            return implicit == type.implicit && name.equals(type.name) && field.equals(type.field);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, field, implicit);
        }

        @Override
        public String toString() {
            return getName();
        }
    }

}
