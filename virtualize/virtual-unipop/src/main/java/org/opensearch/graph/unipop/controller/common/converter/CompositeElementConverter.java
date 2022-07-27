package org.opensearch.graph.unipop.controller.common.converter;

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





import javaslang.collection.Stream;

import java.util.Collections;

public class CompositeElementConverter<TElementSource, TElementDest> implements ElementConverter<TElementSource, TElementDest> {
    //region Constructors
    @SafeVarargs
    public CompositeElementConverter(ElementConverter<TElementSource, TElementDest>...elementConverters) {
        this(Stream.of(elementConverters));
    }

    public CompositeElementConverter(Iterable<ElementConverter<TElementSource, TElementDest>> elementConverters) {
        this.elementConverters = Stream.ofAll(elementConverters).toJavaList();
    }
    //endregion

    //region ElementConverter Implementation
    @Override
    public Iterable<TElementDest> convert(TElementSource tElementSource) {
        for(ElementConverter<TElementSource, TElementDest> elementConverter : this.elementConverters) {
            Iterable<TElementDest> elementsDest = elementConverter.convert(tElementSource);
            if (elementsDest != null) {
                return elementsDest;
            }
        }

        return Collections.emptyList();
    }
    //endregion

    //region Fields
    private Iterable<ElementConverter<TElementSource, TElementDest>> elementConverters;
    //endregion
}
