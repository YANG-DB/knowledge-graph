package org.opensearch.graph.services.engine2;

import org.opensearch.graph.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import org.opensearch.graph.services.GraphApp;
import org.opensearch.graph.services.engine2.data.JoinE2EEpbMockIT;
import org.jooby.Jooby;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.opensearch.graph.test.BaseSuiteMarker;
import org.opensearch.graph.test.framework.index.GlobalSearchEmbeddedNode;
import org.opensearch.graph.test.framework.index.SearchEmbeddedNode;

import java.io.File;
import java.nio.file.Paths;

/**
 * Created by Roman on 21/06/2017.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        JoinE2EEpbMockIT.class
})
public class JoinE2EEpbMockTestSuite implements BaseSuiteMarker {

    @BeforeClass
    public static void setup() throws Exception {
        System.out.println("SmartEpbRedundantTestSuite start");
        start = System.currentTimeMillis();

        elasticEmbeddedNode = GlobalSearchEmbeddedNode.getInstance("Dragons");

        app = new GraphApp(new DefaultAppUrlSupplier("/fuse"))
                .conf(new File(Paths.get("src", "test", "conf", "application.engine2.dev.M2.conf").toString()), "m2.mockEpb");

        app.start("server.join=false");
    }

    @AfterClass
    public static void cleanup() throws Exception {
        if (app != null) {
            app.stop();
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("JoinE2EEpbMockTestSuite elapsed: " + elapsed);
    }

    //region Fields
    private static long start;
    private static Jooby app;
    public static SearchEmbeddedNode elasticEmbeddedNode;
    //endregion
}