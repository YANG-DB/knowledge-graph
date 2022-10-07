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
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.opensearch.graph.model.ontology.Property;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

public interface GraphElementPropertySchema {
    String getName();
    String getType();
    String getpType();

    Iterable<IndexingSchema> getIndexingSchemes();

    <T extends IndexingSchema> Optional<T> getIndexingSchema(IndexingSchema.Type type);

    GraphElementPropertySchema addIndexSchema(IndexingSchema indexingSchema);

    GraphElementPropertySchema addIndexSchemas(Iterable<IndexingSchema> indexingSchemes, Predicate<IndexingSchema> predicate);

    interface IndexingSchema {
        enum Type {
            nested,
            exact,
            ngrams,
            edgeNgrams
        }

        Type getType();
        String getName();

        abstract class Impl implements IndexingSchema {
            //region Constructors
            public Impl(Type type, String name) {
                this.type = type;
                this.name = name;
            }
            //endregion

            //region IndexingSchema Implementation
            @Override
            public Type getType() {
                return this.type;
            }

            @Override
            public String getName() {
                return this.name;
            }
            //endregion

            //region Fields
            private Type type;
            private String name;
            //endregion
        }
    }

    interface ExactIndexingSchema extends IndexingSchema {
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @JsonIgnoreProperties(ignoreUnknown = true)
        class Impl extends IndexingSchema.Impl implements ExactIndexingSchema {
            //region Constructors
            public Impl(String name) {
                super(Type.exact, name);
            }
            //endregion
        }
    }
    interface NestedIndexingSchema extends IndexingSchema {
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @JsonIgnoreProperties(ignoreUnknown = true)
        class Impl extends IndexingSchema.Impl implements ExactIndexingSchema {
            //region Constructors
            public Impl(String path) {
                super(Type.nested, path);
            }
            //endregion
        }
    }

    interface NgramsIndexingSchema extends IndexingSchema {
        int getMaxSize();

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @JsonIgnoreProperties(ignoreUnknown = true)
        class Impl extends IndexingSchema.Impl implements NgramsIndexingSchema {
            //region Constructors
            public Impl(String name, int maxSize) {
                super(Type.ngrams, name);
                this.maxSize = maxSize;
            }
            //endregion

            //region NgramsIndexingSchema Implementation
            @Override
            public int getMaxSize() {
                return this.maxSize;
            }
            //endregion

            //region Fields
            private int maxSize;
            //endregion
        }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    class Impl implements GraphElementPropertySchema {
        //region Constructors
        public Impl(String name) {
            this(name, null);
        }

        public Impl(String name, String type) {
            this(name, name,
                    type, Collections.singletonList(
                            new ExactIndexingSchema.Impl(name)));
        }
        public Impl(String name, String pType, String type) {
            this(name, pType,
                    type, Collections.singletonList(
                            new ExactIndexingSchema.Impl(name)));
        }
        public Impl(Property property) {
            this(property.getName(),property.getpType() ,
                    property.getType(), Collections.singletonList(
                            new ExactIndexingSchema.Impl(property.getName())));
        }

        public Impl(String name, String pType, String type, Iterable<IndexingSchema> indexingSchemes) {
            this.name = name;
            this.pType = pType;
            this.type = type;
            this.indexingSchemes = Stream.ofAll(indexingSchemes)
                    .toJavaMap(indexingSchema -> new Tuple2<>(indexingSchema.getType(), indexingSchema));
        }
        //endregion

        //region GraphElementPropertySchema Implementation
        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getType() {
            return this.type;
        }

        public String getpType() {
            return pType;
        }

        @Override
        public Iterable<IndexingSchema> getIndexingSchemes() {
            return this.indexingSchemes.values();
        }

        @Override
        public <T extends IndexingSchema> Optional<T> getIndexingSchema(IndexingSchema.Type type) {
            return Optional.ofNullable((T)this.indexingSchemes.get(type));
        }
        //endregion

        //region Fields
        //represents the local name for the property
        private String name;
        //represents the storage type for the property
        private String type;
        //represents the fully qualified name for the property
        private String pType;

        @Override
        public GraphElementPropertySchema addIndexSchema(IndexingSchema indexingSchema) {
            //only put new type of index schema as for not to override existing ones
            this.indexingSchemes.putIfAbsent(indexingSchema.getType(),indexingSchema);
            return this;
        }

        @Override
        public GraphElementPropertySchema addIndexSchemas(Iterable<IndexingSchema> indexingSchemes,Predicate<IndexingSchema> predicate) {
            StreamSupport.stream(indexingSchemes.spliterator(),false)
                    .filter(predicate::test)
                    .forEach(index->this.indexingSchemes.put(index.getType(),index));
            return this;
        }

        private Map<IndexingSchema.Type, IndexingSchema> indexingSchemes;
        //endregion
    }
}
