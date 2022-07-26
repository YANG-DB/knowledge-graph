package org.opensearch.graph.services.embedded;




import org.opensearch.client.transport.TransportClient;

/**
 * Created by moti on 3/21/2017.
 */
public interface SearchIndexConfigurer {
    void configure(TransportClient client);

}
