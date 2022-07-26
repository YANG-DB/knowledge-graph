package org.opensearch.graph.dispatcher.resource.store;







import java.util.Map;

public interface NodeStatusResource {
    String NODE = "node";

    Map<String, Object> getMetrics(String node);

    Map<String, Object> getMetrics();

    boolean report();
}
