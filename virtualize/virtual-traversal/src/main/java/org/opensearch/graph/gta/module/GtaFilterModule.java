package org.opensearch.graph.gta.module;


import com.google.inject.Binder;
import com.google.inject.name.Names;
import org.opensearch.graph.dispatcher.gta.LoggingPlanTraversalTranslator;
import org.opensearch.graph.dispatcher.modules.ModuleBase;
import org.opensearch.graph.gta.translation.promise.M1FilterPlanTraversalTranslator;
import org.opensearch.graph.dispatcher.gta.PlanTraversalTranslator;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.inject.name.Names.named;

/**
 * Created by lior.perry on 22/02/2017.
 */
public class GtaFilterModule extends ModuleBase {
    //region ModuleBase Implementation
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(PlanTraversalTranslator.class)
                .annotatedWith(Names.named(LoggingPlanTraversalTranslator.planTraversalTranslatorParameter))
                .to(M1FilterPlanTraversalTranslator.class)
                .asEagerSingleton();
        binder.bind(Logger.class)
                .annotatedWith(named(LoggingPlanTraversalTranslator.loggerParameter))
                .toInstance(LoggerFactory.getLogger(M1FilterPlanTraversalTranslator.class));
        binder.bind(PlanTraversalTranslator.class)
                .to(LoggingPlanTraversalTranslator.class)
                .asEagerSingleton();
    }
    //endregion
}
