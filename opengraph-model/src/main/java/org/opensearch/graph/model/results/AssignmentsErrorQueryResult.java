package org.opensearch.graph.model.results;

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
import org.opensearch.graph.model.resourceInfo.GraphError;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssignmentsErrorQueryResult extends AssignmentsQueryResult {
    private GraphError error;

    public AssignmentsErrorQueryResult(GraphError error) {
        this.error = error;
    }

    @Override
    public int getSize() {
        return -1;
    }

    public GraphError error() {
        return error;
    }

    @Override
    public String toString() {
        return error().getErrorDescription();
    }

    @Override
    public String content() {
        return toString();
    }
}
