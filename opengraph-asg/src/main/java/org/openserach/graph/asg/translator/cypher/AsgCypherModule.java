package org.openserach.graph.asg.translator.cypher;




import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.typesafe.config.Config;
import org.opensearch.graph.dispatcher.modules.ModuleBase;
import org.opensearch.graph.dispatcher.query.QueryTransformer;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.query.QueryInfo;
import org.jooby.Env;

import static com.google.inject.name.Names.named;

public class AsgCypherModule extends ModuleBase {
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(CypherAsgStrategyRegistrar.class)
                .to(M1CypherAsgStrategyRegistrar.class)
                .asEagerSingleton();

        binder.bind(new TypeLiteral<QueryTransformer<QueryInfo<String>, AsgQuery>>(){})
                .annotatedWith(named(AsgCypherTransformer.transformerName))
                .to(AsgCypherTransformer.class)
                .asEagerSingleton();

    }

}
