package org.opensearch.graph.model.transport;

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





import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.transport.cursor.CreateCursorRequest;

public interface CreateQueryRequestMetadata<T> {
    String TYPE_V1QL = "v1";
    String TYPE_CYPHERQL = "cypher";
    String TYPE_GRAPHQL = "graphQL";
//    String TYPE_SPARQL = "sparkQL";

    boolean isSearchPlan();

    String getId();

    String getName();

    String getOntology();

    T getQuery();

    StorageType getStorageType();

    String getType();

    /**
     * Type of physical query -
     *  - concrete - to be executed
     *  - parameterized - to be saved as parameterized template
     * @return
     */
    QueryType getQueryType();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    CreateCursorRequest getCreateCursorRequest();

    long getTtl();

    PlanTraceOptions getPlanTraceOptions();

    enum QueryLanguage {
        v1,cypher,sparql,graphql
    }

    enum StorageType {
        _stored,
        _volatile;
    }

    enum QueryType {
        concrete,
        parameterized


    }
}

