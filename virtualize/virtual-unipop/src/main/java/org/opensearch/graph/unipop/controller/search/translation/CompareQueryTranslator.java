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
import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.apache.tinkerpop.gremlin.process.traversal.P;

public class CompareQueryTranslator implements PredicateQueryTranslator {
    //region Constructors
    public CompareQueryTranslator() {

    }

    public CompareQueryTranslator(boolean shouldAggregateRange) {
        this.shouldAggregateRange = shouldAggregateRange;
    }
    //endregion

    //region PredicateQueryTranslator Implementation
    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, AggregationBuilder aggregationBuilder, String key, P<?> predicate) {
        Compare compare = (Compare) predicate.getBiPredicate();
        String rangeName = shouldAggregateRange ? key : null;
        switch (compare) {
            case eq:
                queryBuilder.push().term(key, getPredicateValue(predicate)).pop();
                break;
            case neq:
                queryBuilder.push().bool().mustNot().term(key, getPredicateValue(predicate)).pop();
                break;
            case gt:
                queryBuilder.push().range(rangeName, key).from(getPredicateValue(predicate)).includeLower(false).pop();
                break;
            case gte:
                queryBuilder.push().range(rangeName, key).from(getPredicateValue(predicate)).includeLower(true).pop();
                break;
            case lt:
                queryBuilder.push().range(rangeName, key).to(getPredicateValue(predicate)).includeUpper(false).pop();
                break;
            case lte:
                queryBuilder.push().range(rangeName, key).to(getPredicateValue(predicate)).includeUpper(true).pop();
                break;
        }

        return queryBuilder;
    }

    private String getPredicateValue(P<?> predicate) {
        return predicate.getValue().toString();
    }
    //endregion

    @Override
    public boolean test(String key, P<?> predicate) {
        return (predicate != null) && (predicate.getBiPredicate() instanceof Compare);
    }

    //region Fields
    private boolean shouldAggregateRange;
    //endregion
}
