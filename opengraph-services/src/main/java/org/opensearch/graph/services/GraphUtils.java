package org.opensearch.graph.services;


import org.opensearch.graph.services.embedded.SearchEngineEmbeddedNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import javaslang.Tuple2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class GraphUtils {
    public static final String OPENSEARCH_EMBEDDED = "opensearch.embedded";
    public static final String OPENSEARCH_CLUSTER_NAME = "opensearch.cluster_name";
    public static final String OPENSEARCH_DELETE_DATA_ON_LOAD = "opensearch.delete_data_on_load";
    public static final String OPENSEARCH_PORT = "opensearch.port ";
    public static final String OPENSEARCH_WORKING_DIR = "opensearch.workingDir";

    private static List<AutoCloseable> closeables = new ArrayList<>();

    public static Config loadConfig(File file, String activeProfile, Tuple2<String, ConfigValue> ... values) {
        Config config = ConfigFactory.parseFile(file);
        config = config.withValue("application.profile", ConfigValueFactory.fromAnyRef(activeProfile, "FuseApp"));
        for (Tuple2<String, ConfigValue> value : values) {
            config = config.withValue(value._1, value._2);
        }
        return config;
    }

    public static boolean loadEmbedded(Config config) throws Exception {
        String nodeName = config.getString(OPENSEARCH_CLUSTER_NAME);
        boolean deleteOnLoad = true;
        if(config.hasPath(OPENSEARCH_DELETE_DATA_ON_LOAD)) {
            deleteOnLoad = config.getBoolean(OPENSEARCH_DELETE_DATA_ON_LOAD);
        }
        int nodePort = config.getInt(OPENSEARCH_PORT);
        String target =  "target/es";
        if(config.hasPath(OPENSEARCH_WORKING_DIR))
            target = config.getString(OPENSEARCH_WORKING_DIR);

        System.out.println(String.format("Loading opensearch (embedded?%b) server %s on port %d on target %s",config.getBoolean("opensearch.embedded"),nodeName,nodePort,target));
        closeables.add(new SearchEngineEmbeddedNode(target, 9200, nodePort, nodeName, deleteOnLoad));
        return true;
    }

    public static void onStop() {
        System.out.println("Stopping all closeables");
        closeables.forEach(c-> {
            try {
                c.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void onStart() {
        System.out.println("Embedded Engine onStart");

    }

    public static void onStarted() {
        System.out.println("Embedded Engine onStarted");
    }
}
