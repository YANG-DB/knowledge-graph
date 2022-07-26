package org.opensearch.graph.test.framework.index;





import org.opensearch.client.Client;
import org.opensearch.client.core.MainResponse;
import org.opensearch.client.transport.TransportClient;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.transport.TransportAddress;
import org.opensearch.graph.test.framework.TestUtil;
import org.opensearch.node.InternalSettingsPreparer;
import org.opensearch.node.Node;
import org.opensearch.plugins.Plugin;
import org.opensearch.transport.Netty4Plugin;
import org.opensearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class SearchEmbeddedNode implements AutoCloseable {
    public static final String GRAPH_TEST_OPENSEARCH = "graph.test_opensearch";

    static {
        //see https://github.com/testcontainers/testcontainers-java/issues/1009 issue with netty & E/S
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    //region PluginConfigurableNode Implementation
    private static class PluginConfigurableNode extends Node {
        public PluginConfigurableNode(Settings settings, Collection<Class<? extends Plugin>> classpathPlugins, Path path, String nodeName) {
            super(InternalSettingsPreparer.prepareEnvironment(settings, new HashMap<>(), path, () -> nodeName), classpathPlugins, false);
        }

/*
        @Override
        protected void registerDerivedNodeNameWithLogger(String nodeName) {
            LogConfigurator.loadLog4jPlugins();
            LogConfigurator.setNodeName(nodeName);
        }
*/
    }
    //endregion

    //region Members
    private static String esWorkingDir;
    private static int numberOfShards;
    public Node node;

    static String nodeName = GRAPH_TEST_OPENSEARCH;

    static int httpPort = 9200;
    static int httpTransportPort = 9300;

    static TransportClient client = null;
    //endregion

    //region Constructors
    SearchEmbeddedNode(String clusterName) throws Exception {
        this(Settings.EMPTY, "target/es", 9200, 9300, clusterName);
    }

    SearchEmbeddedNode(Settings settings, String clusterName, int numberOfShards) throws Exception {
        this(settings, "target/es", 9200, 9300, clusterName, numberOfShards);
    }

    SearchEmbeddedNode() throws Exception {
        this(Settings.EMPTY, "target/es", 9200, 9300, "graph.test_opensearch");
    }

    SearchEmbeddedNode(Settings settings, EngineIndexConfigurer... configurers) throws Exception {
        this(settings, "target/es", 9200, 9300, "graph.test_opensearch", configurers);
    }

    SearchEmbeddedNode(Settings settings, String esWorkingDir, int httpPort, int httpTransportPort, String nodeName, EngineIndexConfigurer... configurers) throws Exception {
        this(settings, esWorkingDir, httpPort, httpTransportPort, nodeName, 1, configurers);
    }

    SearchEmbeddedNode(Settings settings) throws Exception {
        this(settings, esWorkingDir, httpPort, httpTransportPort, nodeName, 1);
    }

    SearchEmbeddedNode(Settings additional, String esWorkingDir, int httpPort, int httpTransportPort, String nodeName, int numberOfShards, EngineIndexConfigurer... configurers) throws Exception {
        SearchEmbeddedNode.httpTransportPort = httpTransportPort;
        SearchEmbeddedNode.nodeName = nodeName;
        SearchEmbeddedNode.esWorkingDir = esWorkingDir;
        SearchEmbeddedNode.httpPort = httpPort;
        SearchEmbeddedNode.numberOfShards = numberOfShards;
        prepare(additional);

        for (EngineIndexConfigurer configurer : configurers) {
            configurer.configure(getClient(nodeName, httpTransportPort));
        }
    }

    //endregion

    //region Methods
    public static TransportClient getClient() {
        return getClient(nodeName, httpTransportPort);
    }

    //region Methods

    public static Client getClient(MainResponse info) {
        if (client == null) {
            try {
                System.out.println("Setting client " + info.getNodeName());
                Settings settings = Settings.builder()
                        .put("cluster.name", info.getNodeName())
                        .put("node.name", info.getClusterName())
                        .build();
                client = new PreBuiltTransportClient(settings)
                        .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), httpTransportPort));
            } catch (UnknownHostException e) {
                throw new UnknownError(e.getMessage());
            }
        }

        return client;
    }

    public static TransportClient getClient(String nodeName) {
        return getClient(nodeName, httpTransportPort);
    }

    public static TransportClient getClient(String nodeName, int httpTransportPort) {
        if (client == null) {
            try {
                System.out.println("Setting client " + nodeName);
                Settings settings = Settings.builder()
                        .put("cluster.name", nodeName)
                        .put("node.name", nodeName)
                        .build();
                client = new PreBuiltTransportClient(settings)
                        .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), httpTransportPort));
            } catch (UnknownHostException e) {
                throw new UnknownError(e.getMessage());
            }
        }

        return client;
    }

    @Override
    public void close() throws Exception {
        System.out.println("Closing E/S embedded");
        closeClient();
        if (this.node != null) {
            this.node.close();
            this.node = null;
        }


        TestUtil.deleteFolder(esWorkingDir);
    }

    public static void closeClient() {
        if (client != null) {
            try {
                client.close();
                client = null;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void prepare(Settings additional) throws Exception {
        this.close();

        Settings settings = getBuilder(additional);

        System.out.println("Setting E/S embedded "+nodeName);
        Collection<Class<? extends Plugin>> plugins = new ArrayList<>();
        plugins.add(Netty4Plugin.class);
        this.node = new PluginConfigurableNode(settings, plugins, Paths.get(esWorkingDir), nodeName);
        this.node = this.node.start();
        System.out.println("Started E/S Embedded");

    }

    public static Settings getBuilder(Settings additional) {
        return Settings.builder()
                .put("cluster.name", nodeName)
                .put("node.name", nodeName)
                .put("path.home", esWorkingDir)
                .put("path.data", esWorkingDir)
                .put("path.logs", esWorkingDir)
                .put("http.port", httpPort)
                .put("transport.type", "netty4")
                .put("http.type", "netty4")
                .put("http.cors.enabled", "true")
//                .put("script.auto_reload_enabled", "false")
                .put("transport.port", httpTransportPort)
                .put(additional)
                .build();
    }
    //endregion

    public static boolean isAvailable(int portNr) {
        boolean portFree;
        try (ServerSocket ignored = new ServerSocket(portNr)) {
            portFree = true;
        } catch (IOException e) {
            portFree = false;
        }
        return portFree;
    }
}
