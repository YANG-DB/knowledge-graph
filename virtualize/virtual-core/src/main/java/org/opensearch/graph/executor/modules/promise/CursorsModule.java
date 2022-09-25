package org.opensearch.graph.executor.modules.promise;

/*-
 * #%L
 * virtual-core
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
import com.google.inject.multibindings.Multibinder;
import org.opensearch.graph.dispatcher.cursor.CompositeCursorFactory;
import org.opensearch.graph.dispatcher.modules.ModuleBase;
import org.opensearch.graph.executor.cursor.promise.TraversalCursor;
import org.opensearch.graph.model.transport.cursor.CreatePathsCursorRequest;
import com.typesafe.config.Config;
import org.jooby.Env;

public class CursorsModule extends ModuleBase {
    //region ModuleBase Implementation
    @Override
    protected void configureInner(Env env, Config config, Binder binder) throws Throwable {
        Multibinder.newSetBinder(binder, CompositeCursorFactory.Binding.class).addBinding().toInstance(new CompositeCursorFactory.Binding(
                CreatePathsCursorRequest.CursorType,
                CreatePathsCursorRequest.class,
                new TraversalCursor.Factory()));
    }
    //endregion
}
