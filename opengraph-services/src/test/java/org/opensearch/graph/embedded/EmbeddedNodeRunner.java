package org.opensearch.graph.embedded;


// TODO - use this test to make use of open search test framework

//import com.carrotsearch.randomizedtesting.RandomizedRunner;
//import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.typesafe.config.Config;
import org.opensearch.graph.services.GraphRunner;
import org.opensearch.graph.services.GraphUtils;
import org.junit.Test;
//import org.junit.runner.RunWith;

import java.io.File;
import java.net.URL;

//@RunWith(RandomizedRunner.class)
//@ThreadLeakScope(ThreadLeakScope.Scope.NONE)
public class EmbeddedNodeRunner {

    public static final String applicationConfFilename = "config/application.conf";
    public static final String activeProfile = "activeProfile";
    public static final String logbackConfigurationFilename = "config/logback.xml";

    @Test
    public void runEmbeddedNodeTest() throws Exception {
        final GraphRunner.Options options = new GraphRunner.Options(applicationConfFilename, activeProfile, logbackConfigurationFilename, true, true);
        String confFilename = options.getApplicationConfFilename() != null ? options.getApplicationConfFilename() : "application.conf";
        File configFile = new File(confFilename);
        if (!configFile.exists()) {
            System.out.println("ConfigFile  " + confFilename + " Not Found - fallback getTo application.conf");
            final URL resource = Thread.currentThread().getContextClassLoader().getResource(applicationConfFilename);
            configFile = new File(resource.getFile());
        }
        Config config = GraphUtils.loadConfig(configFile, options.getActiveProfile());
        final boolean embedded = GraphUtils.loadEmbedded(config);
        Thread.currentThread().join();
    }
}
