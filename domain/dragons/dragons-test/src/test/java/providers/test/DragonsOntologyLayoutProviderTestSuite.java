package providers.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.opensearch.graph.test.BaseSuiteMarker;

/**
 * Created by Roman on 21/06/2017.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        DragonsOntologyGraphLayoutProviderFactoryIT.class
})
public class DragonsOntologyLayoutProviderTestSuite implements BaseSuiteMarker {
    @BeforeClass
    public static void setup() throws Exception {
        System.out.println("DragonsOntologyLayoutProviderTestSuite start");
    }

    @AfterClass
    public static void cleanup() throws Exception {
        System.out.println("DragonsOntologyLayoutProviderTestSuite finished");

    }

}
