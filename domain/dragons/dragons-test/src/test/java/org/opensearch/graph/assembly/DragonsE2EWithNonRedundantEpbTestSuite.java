package org.opensearch.graph.assembly;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.opensearch.graph.assembly.queries.DragonsNestedNoConstraintsQueryIT;
import org.opensearch.graph.assembly.queries.DragonsSimpleConstraintsQueryIT;
import org.opensearch.graph.assembly.queries.DragonsSimpleFileUploadIT;
import org.opensearch.graph.assembly.queries.DragonsSimpleNoConstraintsQueryIT;
import org.opensearch.graph.test.BaseSuiteMarker;

import java.nio.file.Paths;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        DragonsSimpleFileUploadIT.class,
        DragonsSimpleNoConstraintsQueryIT.class,
        DragonsSimpleConstraintsQueryIT.class,
        DragonsNestedNoConstraintsQueryIT.class
})
public class DragonsE2EWithNonRedundantEpbTestSuite implements BaseSuiteMarker {

    @BeforeClass
    public static void setup() throws Exception {
        System.out.println("DragonsE2EWithNonRedundantEpbTestSuite - setup");
        Setup.withPath(Paths.get( "src","resources", "assembly", "Dragons", "config", "application.test.engine3.m1.dfs.dragons.public.conf"));
        Setup.setup();//DO NOT REMOVE
    }

    @AfterClass
    public static void tearDown() throws Exception {
        System.out.println("DragonsE2EWithNonRedundantEpbTestSuite - teardown");
//        Setup.cleanup();
    }
}
