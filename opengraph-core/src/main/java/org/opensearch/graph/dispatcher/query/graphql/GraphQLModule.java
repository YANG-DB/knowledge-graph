
package org.opensearch.graph.dispatcher.query.graphql;



import com.google.inject.Binder;
import com.typesafe.config.Config;
import org.opensearch.graph.dispatcher.modules.ModuleBase;
import org.jooby.Env;

public class GraphQLModule extends ModuleBase {

    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(GraphQLSchemaUtils.class)
                .to(GraphQLToOntologyTransformer.class)
                .asEagerSingleton();
        binder.bind(GraphQL2QueryTransformer.class)
                .asEagerSingleton();
        binder.bind(GraphQLToOntologyTransformer.class)
                .asEagerSingleton();


    }

    //endregion
}
