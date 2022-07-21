package org.opensearch.graph.stats.es.client;



import org.apache.commons.configuration.Configuration;
import org.opensearch.client.transport.TransportClient;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.transport.TransportAddress;
import org.opensearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientProvider {

    private ClientProvider() {
        throw new IllegalAccessError("Utility class");
    }

    //region Static Methods
    public static TransportClient getDataClient(Configuration configuration) throws UnknownHostException {
        String clusterName = configuration.getString("es.cluster.name");
        int transportPort = configuration.getInt("es.client.transport.port");
        String[] hosts = configuration.getStringArray("es.nodes.hosts");

        return getTransportClient(clusterName, transportPort, hosts);
    }

    public static TransportClient getStatClient(Configuration configuration) throws UnknownHostException {
        String clusterName = configuration.getString("statistics.cluster.name");
        int transportPort = configuration.getInt("statistics.client.transport.port");
        String[] hosts = configuration.getStringArray("statistics.nodes.hosts");

        return getTransportClient(clusterName, transportPort, hosts);
    }

    public static TransportClient getTransportClient(String clusterName, int transportPort, String[] hosts) throws UnknownHostException {
        Settings settings = Settings.builder().put("cluster.name", clusterName).build();
        TransportClient esClient = new PreBuiltTransportClient(settings);
        for(String node: hosts) {
            esClient.addTransportAddress(new TransportAddress(InetAddress.getByName(node), transportPort));
        }
        return esClient;
    }
    //endregion
}
