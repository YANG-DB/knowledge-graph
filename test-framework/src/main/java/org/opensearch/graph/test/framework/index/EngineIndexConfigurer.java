package org.opensearch.graph.test.framework.index;


import org.opensearch.client.transport.TransportClient;

/**
 * Created by moti on 3/21/2017.
 */
public interface EngineIndexConfigurer {
    void configure(TransportClient client);

}
