package org.opensearch.graph.executor.opensearch;


import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import org.opensearch.graph.executor.mock.opensearch.MockClient;
import org.opensearch.graph.unipop.controller.OpensearchGraphConfiguration;
import javaslang.collection.Stream;
import org.opensearch.client.Client;
import org.opensearch.client.transport.TransportClient;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.transport.TransportAddress;
import org.opensearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by roman.margolis on 04/01/2018.
 */
public class ClientProvider implements Provider<Client> {
    public static final String createMockParameter = "ClientProvider.@createMock";

    @Inject
    //region Constructors
    public ClientProvider(
            @Named(createMockParameter) boolean createMock,
            OpensearchGraphConfiguration configuration) {
        this.createMock = createMock;
        this.configuration = configuration;
    }
    //endregion

    //region Provider Implementation
    @Override
    public Client get() {
        if (this.createMock) {
            System.out.println("Using mock opensearch client!");
            return new MockClient();
        }

        Settings settings = Settings.builder()
                .put("cluster.name", this.configuration.getClusterName())
                .put("client.transport.ignore_cluster_name", this.configuration.getClientTransportIgnoreClusterName())
                .build();
        TransportClient client = new PreBuiltTransportClient(settings);
        Stream.of(this.configuration.getClusterHosts()).forEach(host -> {
            try {
                client.addTransportAddress(new TransportAddress(InetAddress.getByName(host), this.configuration.getClusterPort()));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        });

        return client;
    }
    //endregion

    //region Fields
    private boolean createMock;
    private OpensearchGraphConfiguration configuration;
    //endregion
}
