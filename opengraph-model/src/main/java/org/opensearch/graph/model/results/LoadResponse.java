package org.opensearch.graph.model.results;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.resourceInfo.FuseError;

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

    LoadResponse response(LoadResponse.CommitResponse<String, FuseError> response);

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
    class LoadResponseImpl implements LoadResponse<String, FuseError> {


        @JsonInclude(JsonInclude.Include.NON_NULL)
        private List<CommitResponse<String, FuseError>> responses;

        public LoadResponseImpl() {
            this.responses = new ArrayList<>();
        }

        public LoadResponse response(CommitResponse<String, FuseError> response) {
            this.responses.add(response);
            return this;
        }

        @Override
        public List<CommitResponse<String, FuseError>> getResponses() {
            return responses;
        }
    }

    /**
     * create an assignment entity to repost the loading results
     * @param load
     * @return
     */
    static AssignmentCount buildAssignment(LoadResponse<String, FuseError> load) {
        Long success = load.getResponses().stream().map(r -> r.getSuccesses().size()).count();
        Long failed = load.getResponses().stream().map(r -> r.getFailures().size()).count();
        Map<String, AtomicLong> results = new HashMap<>();
        results.put("Success",new AtomicLong(success));
        results.put("Failed",new AtomicLong(failed));
        AssignmentCount assignmentCount = new AssignmentCount(results);
        return assignmentCount;
    }


}
