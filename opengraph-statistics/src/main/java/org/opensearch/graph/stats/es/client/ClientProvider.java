package org.opensearch.graph.stats.es.client;

/*-
 * #%L
 * opengraph-statistics
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
