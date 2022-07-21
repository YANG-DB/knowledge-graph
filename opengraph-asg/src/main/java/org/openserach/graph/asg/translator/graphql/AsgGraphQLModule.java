package org.openserach.graph.asg.translator.graphql;



import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.typesafe.config.Config;
import org.opensearch.graph.dispatcher.modules.ModuleBase;
import org.opensearch.graph.dispatcher.query.QueryTransformer;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.query.QueryInfo;
import org.jooby.Env;

import static com.google.inject.name.Names.named;
import static org.openserach.graph.asg.translator.graphql.AsgGraphQLTransformer.transformerName;

/**
 * Created by lior.perry on 22/02/2017.
 */
public class AsgGraphQLModule extends ModuleBase {

    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(new TypeLiteral<QueryTransformer<QueryInfo<String>, AsgQuery>>(){})
                .annotatedWith(named(transformerName))
                .to(AsgGraphQLTransformer.class)
                .asEagerSingleton();

    }

}
