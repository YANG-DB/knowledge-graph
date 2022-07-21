package org.opensearch.graph.model.transport;




import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ExecutionScope {
    public static final String clientParameter = "ExecutionScope.@maxExecutionTimeout";

    public ExecutionScope() {}

    @Inject
    public ExecutionScope(@Named(clientParameter) long timeout) {
        this.timeout = timeout;
    }

    public long getTimeout() {
        return timeout;
    }

    private long timeout = 600 * 1000 * 3;

}
