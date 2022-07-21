package org.openserach.graph.asg.translator.sparql;



import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.typesafe.config.Config;
import org.opensearch.graph.dispatcher.modules.ModuleBase;
import org.opensearch.graph.dispatcher.query.QueryTransformer;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.query.QueryInfo;
import org.jooby.Env;
import org.openserach.graph.asg.strategy.SparqlAsgStrategyRegistrar;

import static com.google.inject.name.Names.named;

public class AsgSparqlModule extends ModuleBase {
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(SparqlAsgStrategyRegistrar.class)
                .to(M1SparqlAsgStrategyRegistrar.class)
                .asEagerSingleton();

        binder.bind(new TypeLiteral<QueryTransformer<QueryInfo<String>, AsgQuery>>(){})
                .annotatedWith(named(AsgSparQLTransformer.transformerName))
                .to(AsgSparQLTransformer.class)
                .asEagerSingleton();

    }

}
