package org.opensearch.graph.utils;

import org.opensearch.graph.client.BaseGraphClient;
import org.opensearch.graph.client.GraphClient;
import org.opensearch.graph.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import org.opensearch.graph.services.GraphApp;

import java.io.File;
import java.nio.file.Paths;

public class EngineManager {

    public EngineManager(String confFile, String activeProfile) {
        this.confFile = confFile;
        this.activeProfile = activeProfile;
    }

    public void init() throws Exception {
        startFuse();
    }

    public void cleanup() {
        teardownFuse();
    }

    private void teardownFuse() {
        if (graphApp != null) {
            graphApp.stop();
        }
    }

    private void startFuse() throws Exception {

        graphApp = new GraphApp(new DefaultAppUrlSupplier("/fuse"))
                .conf(new File(Paths.get("fuse-test", "fuse-benchmarks-test", "src", "main", "resources", "conf", confFile).toString()), activeProfile);

        graphApp.start("server.join=false");

        graphClient = new BaseGraphClient("http://localhost:8888/fuse");
    }

    public GraphApp getGraphApp() {
        return graphApp;
    }

    public GraphClient getGraphClient() {
        return graphClient;
    }

    private String confFile;
    private String activeProfile;

    private GraphApp graphApp;
    private GraphClient graphClient;


}
