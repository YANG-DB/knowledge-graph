
package org.opensearch.graph.dispatcher.query.sql;




import com.google.inject.Binder;
import com.typesafe.config.Config;
import org.opensearch.graph.dispatcher.modules.ModuleBase;
import org.jooby.Env;

public class SqlModule extends ModuleBase {

    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(DDLToOntologyTransformer.class).asEagerSingleton();
    }

    //endregion
}
