package org.opensearch.graph.unipop.controller.common.appender;

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





import org.opensearch.graph.unipop.controller.common.context.ElementControllerContext;
import org.opensearch.graph.unipop.controller.search.SearchBuilder;
import javaslang.collection.Stream;

import java.util.List;

public class MustFetchSourceSearchAppender implements SearchAppender<ElementControllerContext> {
    //region Constructors
    public MustFetchSourceSearchAppender(String...mustFetchFields) {
        this(Stream.of(mustFetchFields));
    }

    public MustFetchSourceSearchAppender(Iterable<String> mustFetchFields) {
        this.mustFetchFields = Stream.ofAll(mustFetchFields).toJavaList();
    }
    //endregion

    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, ElementControllerContext context) {
        searchBuilder.getIncludeSourceFields().addAll(this.mustFetchFields);
        return true;
    }
    //endregion

    //region Fields
    private List<String> mustFetchFields;
    //endregion
}
