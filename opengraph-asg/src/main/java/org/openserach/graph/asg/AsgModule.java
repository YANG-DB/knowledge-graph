package org.openserach.graph.asg;

/*-
 * #%L
 * opengraph-asg
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
import com.google.inject.TypeLiteral;
import com.typesafe.config.Config;
import org.openserach.graph.asg.strategy.AsgStrategyRegistrar;
import org.openserach.graph.asg.strategy.M1AsgStrategyRegistrar;
import org.opensearch.graph.dispatcher.asg.QueryToCompositeAsgTransformer;
import org.opensearch.graph.dispatcher.modules.ModuleBase;
import org.opensearch.graph.dispatcher.query.QueryTransformer;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.query.Query;
import org.jooby.Env;

/**
 * Created by lior.perry on 22/02/2017.
 */
public class AsgModule extends ModuleBase {
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(AsgStrategyRegistrar.class)
                .to(getAsgStrategyRegistrar(conf));

        binder.bind(new TypeLiteral<QueryTransformer<Query, AsgQuery>>(){})
                .to(QueryToCompositeAsgTransformer.class)
                .asEagerSingleton();

        binder.bind(new TypeLiteral<QueryTransformer<AsgQuery, AsgQuery>>(){})
                .to(AsgQueryTransformer.class)
                .asEagerSingleton();

    }

    protected Class<? extends AsgStrategyRegistrar> getAsgStrategyRegistrar(Config conf) throws ClassNotFoundException {
        return M1AsgStrategyRegistrar.class;
    }
}
