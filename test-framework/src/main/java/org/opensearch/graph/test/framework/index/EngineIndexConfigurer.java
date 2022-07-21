package org.opensearch.graph.test.framework.index;



import org.opensearch.client.transport.TransportClient;

public interface EngineIndexConfigurer {
    void configure(TransportClient client);

}
