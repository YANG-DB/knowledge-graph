package org.opensearch.graph.unipop.controller.search.translation;

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





import org.opensearch.graph.unipop.controller.search.AggregationBuilder;
import org.opensearch.graph.unipop.controller.search.QueryBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.unipop.process.predicate.CountFilterP;

import java.util.Collections;

public class CountFilterQueryTranslator implements PredicateQueryTranslator {
    //region PredicateQueryTranslator Implementation
    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, AggregationBuilder aggregationBuilder, String key, P<?> predicate) {
        if (predicate == null) {
            return queryBuilder;
        }

        if (predicate instanceof CountFilterP) {
            CountFilterP filterP = (CountFilterP) predicate;
            String field = key;
            //populate count based filter sub aggregation (bucket-filter type in ES semantics)
            AggregationBuilder countFilter = aggregationBuilder.seek(field).countFilter(field);
            countFilter.field(field);
            countFilter.operator(filterP.getBiPredicate());
            countFilter.operands(Collections.singletonList(filterP.getValue()));
        }


        return queryBuilder;
    }

    @Override
    public boolean test(String key, P<?> predicate) {
        return predicate!=null && predicate.getBiPredicate() instanceof CountFilterP.CountFilterCompare;
    }
    //endregion
}
