
package org.opensearch.graph.dispatcher.query.rdf;



import com.google.inject.Binder;
import com.typesafe.config.Config;
import org.opensearch.graph.dispatcher.modules.ModuleBase;
import org.jooby.Env;

/**
 * Created by lior.perry on 15/02/2017.
 * <p>
 * This module is called by the fuse-service scanner class loader
 */
public class SparqlModule extends ModuleBase {

    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(OWLToOntologyTransformer.class).asEagerSingleton();
    }

    //endregion
}
