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






import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.query.QueryRef;
import org.opensearch.graph.model.query.properties.constraint.NamedParameter;
import org.opensearch.graph.model.transport.cursor.CreateCursorRequest;

import java.util.Collection;
import java.util.Collections;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecuteStoredQueryRequest extends CreateQueryRequest {

    private final Collection<NamedParameter> parameters;
    private final Collection<NamedParameter> executionParams;

    public ExecuteStoredQueryRequest() {
        this.parameters = Collections.EMPTY_LIST;
        this.executionParams = Collections.EMPTY_LIST;
    }

    public ExecuteStoredQueryRequest(String id, String name, Collection<NamedParameter> parameters,Collection<NamedParameter> executionParams) {
        super(id, name, new QueryRef(name));
        this.parameters = parameters;
        this.executionParams = executionParams;
    }

    public ExecuteStoredQueryRequest(String id, String name, CreateCursorRequest createCursorRequest, Collection<NamedParameter> parameters,Collection<NamedParameter> executionParams) {
        super(id, name, new QueryRef(name), new PlanTraceOptions(), createCursorRequest);
        this.parameters = parameters;
        this.executionParams = executionParams;
    }

    public Collection<NamedParameter> getExecutionParams() {
        return executionParams;
    }

    public Collection<NamedParameter> getParameters() {
        return parameters;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public CreatePageRequest getPageCursorRequest() {
        return (getCreateCursorRequest() != null ? getCreateCursorRequest().getCreatePageRequest() : null);
    }
}
