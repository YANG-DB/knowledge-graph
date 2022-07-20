package org.opensearch.graph.gta.module;

/*-
 * #%L
 * virtual-traversal 
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
