package org.opensearch.graph.assembly;

import com.typesafe.config.Config;
import org.opensearch.client.transport.TransportClient;
import org.opensearch.graph.client.BaseGraphClient;
import org.opensearch.graph.client.GraphClient;
import org.opensearch.graph.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import org.opensearch.graph.services.GraphApp;
import org.opensearch.graph.services.GraphUtils;
import org.opensearch.graph.test.framework.index.GlobalSearchEmbeddedNode;
import org.opensearch.graph.test.framework.index.SearchEmbeddedNode;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class Setup {
    public static Path path = Paths.get( "src","resources", "assembly", "Dragons", "config", "application.test.engine3.m1.dfs.dragons.public.conf");
    public static String userDir = Paths.get( "src","resources", "assembly", "Dragons").toFile().getAbsolutePath();

    public static SearchEmbeddedNode elasticEmbeddedNode = null;
    public static GraphApp app = null;
    public static GraphClient GraphClient = null;
    public static TransportClient client = null;

    public static void withPath(Path path) {
        Setup.path = path;
    }

    public static void setup() throws Exception {
        setup(true);
    }

    public static void setup(boolean embedded) throws Exception {
        setup(embedded,true);
    }

    public static void setup(boolean embedded, boolean init) throws Exception {
        init(embedded,init,true);
        GraphClient = new BaseGraphClient("http://localhost:8888/fuse");
    }

    public static void setup(boolean embedded, boolean init,boolean startFuse) throws Exception {
        init(embedded,init,startFuse);
        GraphClient = new BaseGraphClient("http://localhost:8888/fuse");
    }

    public static void setup(boolean embedded, boolean init, boolean startFuse, GraphClient givenGraphClient) throws Exception {
        init(embedded,init,startFuse);
        //set fuse client
        GraphClient = givenGraphClient;
    }

    private static void init(boolean embedded, boolean init, boolean startGraph) throws Exception {
        //set location aware user directory
        System.setProperty("user.dir",userDir);
        // Start embedded ES
        if(embedded) {
            elasticEmbeddedNode = GlobalSearchEmbeddedNode.getInstance("Dragons");
            client = SearchEmbeddedNode.getClient();
        } else {
            //use existing running ES
            client = SearchEmbeddedNode.getClient("Dragons", 9300);
        }

        if(init) {
            //todo some init stuff
        }

        start(startGraph);
    }

    private static void start(boolean startGraph) {
        // Start fuse app (based on Jooby app web server)
        if(startGraph) {
            // Load fuse engine config file
            String confFilePath = path.toString();
            //load configuration
            Config config = GraphUtils.loadConfig(new File(confFilePath),"activeProfile" );
            String[] joobyArgs = new String[]{
                    "logback.configurationFile="+Paths.get("src", "test","resources", "config", "logback.xml").toString() ,
                    "server.join=false"
            };

            app = new GraphApp(new DefaultAppUrlSupplier("/fuse"))
                    .conf(path.toFile(), "activeProfile");
            app.start("server.join=false");
        }
    }


    public static void cleanup() throws Exception {
        if (app != null) {
            app.stop();
        }

        GlobalSearchEmbeddedNode.close();
    }
}
