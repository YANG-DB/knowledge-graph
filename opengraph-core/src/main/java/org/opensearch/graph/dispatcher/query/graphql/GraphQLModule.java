
package org.opensearch.graph.dispatcher.query.graphql;

/*-
 * #%L
 * opengraph-core
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
