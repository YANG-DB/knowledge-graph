package org.opensearch.graph.executor.modules.promise;


import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;
import org.opensearch.graph.dispatcher.cursor.CompositeCursorFactory;
import org.opensearch.graph.dispatcher.modules.ModuleBase;
import org.opensearch.graph.executor.cursor.promise.TraversalCursor;
import org.opensearch.graph.model.transport.cursor.CreatePathsCursorRequest;
import com.typesafe.config.Config;
import org.jooby.Env;

/**
 * Created by Roman on 7/7/2018.
 */
public class CursorsModule extends ModuleBase {
    //region ModuleBase Implementation
    @Override
    protected void configureInner(Env env, Config config, Binder binder) throws Throwable {
        Multibinder.newSetBinder(binder, CompositeCursorFactory.Binding.class).addBinding().toInstance(new CompositeCursorFactory.Binding(
                CreatePathsCursorRequest.CursorType,
                CreatePathsCursorRequest.class,
                new TraversalCursor.Factory()));
    }
    //endregion
}
