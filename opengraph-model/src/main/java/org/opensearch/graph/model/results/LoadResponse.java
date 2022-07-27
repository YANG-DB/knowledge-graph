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

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public interface LoadResponse<S, F> {
    LoadResponse EMPTY = new LoadResponse() {
        @Override
        public List<CommitResponse> getResponses() {
            return Collections.emptyList();
        }

        @Override
        public LoadResponse response(CommitResponse response) {
            return this;
        }
    };

    List<CommitResponse<S, F>> getResponses();

    LoadResponse response(LoadResponse.CommitResponse<String, GraphError> response);

    interface CommitResponse<S, F> {
        CommitResponse EMPTY = new CommitResponse() {
            @Override
            public List getSuccesses() {
                return Collections.emptyList();
            }

            @Override
            public List getFailures() {
                return Collections.emptyList();
            }
        };

        List<S> getSuccesses();

        List<F> getFailures();

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class LoadResponseImpl implements LoadResponse<String, GraphError> {


        @JsonInclude(JsonInclude.Include.NON_NULL)
        private List<CommitResponse<String, GraphError>> responses;

        public LoadResponseImpl() {
            this.responses = new ArrayList<>();
        }

        public LoadResponse response(CommitResponse<String, GraphError> response) {
            this.responses.add(response);
            return this;
        }

        @Override
        public List<CommitResponse<String, GraphError>> getResponses() {
            return responses;
        }
    }

    static AssignmentCount buildAssignment(LoadResponse<String, GraphError> load) {
        Long success = load.getResponses().stream().map(r -> r.getSuccesses().size()).count();
        Long failed = load.getResponses().stream().map(r -> r.getFailures().size()).count();
        Map<String, AtomicLong> results = new HashMap<>();
        results.put("Success",new AtomicLong(success));
        results.put("Failed",new AtomicLong(failed));
        AssignmentCount assignmentCount = new AssignmentCount(results);
        return assignmentCount;
    }


}
