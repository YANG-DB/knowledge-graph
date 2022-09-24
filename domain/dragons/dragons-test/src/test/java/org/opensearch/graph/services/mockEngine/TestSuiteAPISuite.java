package org.opensearch.graph.services.mockEngine;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import org.jooby.Jooby;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.opensearch.graph.dispatcher.cursor.Cursor;
import org.opensearch.graph.dispatcher.cursor.CursorFactory;
import org.opensearch.graph.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import org.opensearch.graph.model.results.AssignmentsQueryResult;
import org.opensearch.graph.services.GraphApp;
import org.opensearch.graph.test.BaseSuiteMarker;

import java.io.File;
import java.nio.file.Paths;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Roman on 21/06/2017.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        ApiDescriptorIT.class,
        CatalogIT.class,
        CursorIT.class,
        CursorCompositeIT.class,
        DataIT.class,
        PageIT.class,
        PlanIT.class,
        QueryIT.class,
        SearchIT.class
})
@Ignore("Fix to match OS engine")
public class TestSuiteAPISuite implements BaseSuiteMarker {
    @BeforeClass
    public static void setup() throws Exception {
        Cursor cursor = mock(Cursor.class);
        when(cursor.getNextResults(anyInt())).thenReturn(AssignmentsQueryResult.Builder.instance().build());

        CursorFactory cursorFactory = mock(CursorFactory.class);
        when(cursorFactory.createCursor(any())).thenReturn(cursor);

        app = new GraphApp(new DefaultAppUrlSupplier("/opengraph"))
                .conf(new File(Paths.get("src", "test", "conf", "application.mockEngine.dev.conf").toString()))
                .injector((stage, module) -> Guice.createInjector(stage, Modules.override(module).with(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(CursorFactory.class).toInstance(cursorFactory);
                    }
                })));

        app.start("server.join=false");

    }

    @AfterClass
    public static void cleanup() {
        if (app != null) {
            app.stop();
        }
    }

    //region Fields
    private static Jooby app;
    //endregion
}
