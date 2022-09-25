package org.opensearch.graph.dragons;

/*-
 * #%L
 * dragons-ext
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
import com.google.inject.multibindings.Multibinder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import org.opensearch.graph.dragons.cursor.LogicalGraphHierarchyTraversalCursor;
import org.opensearch.graph.core.driver.BasicIdGenerator;
import org.opensearch.graph.dispatcher.cursor.CompositeCursorFactory;
import org.opensearch.graph.dispatcher.driver.IdGeneratorDriver;
import org.opensearch.graph.dispatcher.modules.ModuleBase;
import org.opensearch.graph.dispatcher.ontology.DirectoryIndexProvider;
import org.opensearch.graph.dispatcher.ontology.IndexProviderFactory;
import org.opensearch.graph.executor.ontology.schema.load.EntityTransformer;
import org.opensearch.graph.model.Range;
import org.opensearch.graph.model.transport.cursor.LogicalGraphCursorRequest;
import org.jooby.Env;

import java.net.URISyntaxException;

import static com.google.inject.name.Names.named;

public class DragonsModule extends ModuleBase {
    @Override
    protected void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        String indexName = conf.getString(conf.getString("assembly") + ".idGenerator_indexName");
        binder.bindConstant().annotatedWith(named(BasicIdGenerator.indexNameParameter)).to(indexName);
        binder.bind(IndexProviderFactory.class).toInstance(getIndexProvider(conf));
        binder.bind(new TypeLiteral<IdGeneratorDriver<Range>>() {}).to(BasicIdGenerator.class).asEagerSingleton();
        binder.bind(EntityTransformer.class);

        Multibinder<CompositeCursorFactory.Binding> bindingMultibinder = Multibinder.newSetBinder(binder, CompositeCursorFactory.Binding.class);
        bindingMultibinder.addBinding().toInstance(new CompositeCursorFactory.Binding(
                LogicalGraphCursorRequest.CursorType,
                LogicalGraphCursorRequest.class,
                new LogicalGraphHierarchyTraversalCursor.Factory()));

//        binder.bind(GraphLayoutProviderFactory.class).toInstance(new DragonsOntologyGraphLayoutProviderFactory(conf.getString("opengraph.ontology_provider_dir")));
//        binder.bind(DragonsExtensionQueryController.class).in(RequestScoped.class);

    }

    private IndexProviderFactory getIndexProvider(Config conf) throws Throwable{
        try {
            return new DirectoryIndexProvider(conf.getString("opengraph.index_provider_dir"));
        } catch (ConfigException e) {
            return (IndexProviderFactory) Class.forName(conf.getString("opengraph.index_provider")).getConstructor().newInstance();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


}
