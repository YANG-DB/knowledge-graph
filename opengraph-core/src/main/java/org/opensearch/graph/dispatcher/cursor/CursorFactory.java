package org.opensearch.graph.dispatcher.cursor;

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



import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.dispatcher.resource.QueryResource;
import org.opensearch.graph.model.transport.CreatePageRequest;
import org.opensearch.graph.model.transport.cursor.CreateCursorRequest;
import org.opensearch.graph.model.transport.cursor.LogicalGraphCursorRequest;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Roman on 02/04/2017.
 */
public interface CursorFactory {

    String CURSOR_TYPE = "CursorType";

    interface Context<T> {
        T getSchemaProvider();

        OntologyProvider getOntologyProvider();

        QueryResource getQueryResource();

        CreateCursorRequest getCursorRequest();

        class Impl<T> implements Context<T> {
            //region Constructors
            public Impl(T schemaProvider,OntologyProvider ontologyProvider,QueryResource queryResource, CreateCursorRequest cursorRequest) {
                this.schemaProvider = schemaProvider;
                this.ontologyProvider = ontologyProvider;
                this.queryResource = queryResource;
                this.cursorRequest = cursorRequest;
            }
            //endregion

            //region Context Implementation
            public QueryResource getQueryResource() {
                return queryResource;
            }

            public CreateCursorRequest getCursorRequest() {
                return cursorRequest;
            }
            //endregion


            @Override
            public T getSchemaProvider() {
                return schemaProvider;
            }

            @Override
            public OntologyProvider getOntologyProvider() {
                return ontologyProvider;
            }

            private T schemaProvider;
            private OntologyProvider ontologyProvider;
            //region Fields
            private QueryResource queryResource;
            private CreateCursorRequest cursorRequest;
            //endregion
        }
    }

    Cursor createCursor(Context context);

    /**
     * resolve cursor request class name by CursorType value
     * @param cursorTypeName
     * @return
     */
    static Class<? extends CreateCursorRequest> resolve(String cursorTypeName) {
        Reflections reflections = new Reflections(CreateCursorRequest.class.getPackage().getName());
        Set<Class<? extends CreateCursorRequest>> allClasses = reflections.getSubTypesOf(CreateCursorRequest.class);
        Optional<Class<? extends CreateCursorRequest>> cursorType = allClasses.stream().filter(clazz -> {
            try {
                //get value of static field member
                return clazz.getField(CURSOR_TYPE).get(null).equals(cursorTypeName);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
            return false;
        }).findFirst();

        return cursorType.isPresent() ? cursorType.get() : LogicalGraphCursorRequest.class;
    }

    /**
     * generate cursor request based on given params
     * @param cursorTypeName
     * @return
     */
    static CreateCursorRequest request(String ontology,String cursorTypeName, CreatePageRequest pageRequest) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        Class<? extends CreateCursorRequest> cursor = resolve(cursorTypeName);
        Constructor<? extends CreateCursorRequest> constructor = cursor.getConstructor();
        CreateCursorRequest request = constructor.newInstance();
        return request.with(pageRequest).with(ontology);
    }

}
