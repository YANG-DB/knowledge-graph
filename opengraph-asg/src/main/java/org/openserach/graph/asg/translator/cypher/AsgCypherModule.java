package org.openserach.graph.asg.translator.cypher;

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
import org.opensearch.graph.dispatcher.modules.ModuleBase;
import org.opensearch.graph.dispatcher.query.QueryTransformer;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.query.QueryInfo;
import org.jooby.Env;

import static com.google.inject.name.Names.named;

/**
 * Created by lior.perry on 22/02/2017.
 */
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
