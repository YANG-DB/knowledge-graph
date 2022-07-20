package org.opensearch.graph.unipop.controller.promise.appender;

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

import org.opensearch.graph.unipop.controller.OpensearchGraphConfiguration;
import org.opensearch.graph.unipop.controller.common.appender.SearchAppender;
import org.opensearch.graph.unipop.controller.common.context.LimitContext;
import org.opensearch.graph.unipop.controller.search.SearchBuilder;

/**
 * Created by Roman on 13/04/2017.
 */
public class SizeSearchAppender implements SearchAppender<LimitContext> {
    //region Constructors
    public SizeSearchAppender(OpensearchGraphConfiguration configuration) {
        this.configuration = configuration;
    }
    //endregion

    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, LimitContext context) {
        searchBuilder.setLimit(context.getLimit() < 0 ?
                configuration.getElasticGraphDefaultSearchSize() :
                Math.min(context.getLimit(), configuration.getElasticGraphMaxSearchSize()));

        searchBuilder.setScrollSize(configuration.getElasticGraphScrollSize());
        searchBuilder.setScrollTime(configuration.getElasticGraphScrollTime());

        return true;
    }
    //endregion

    //region Fields
    private OpensearchGraphConfiguration configuration;
    //endregion
}
