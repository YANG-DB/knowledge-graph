package org.opensearch.graph.dispatcher.modules;

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
import com.typesafe.config.ConfigException;
import org.opensearch.graph.dispatcher.ontology.*;
import org.opensearch.graph.dispatcher.urlSupplier.AppUrlSupplier;
import org.opensearch.graph.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import org.opensearch.graph.model.resourceInfo.GraphError;
import org.jooby.Env;

import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

public class CoreDispatcherModule extends ModuleBase {

    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(AppUrlSupplier.class).toInstance(getAppUrlSupplier(conf));
        binder.bind(OntologyProvider.class).toInstance(getOntologyProvider(conf));
        binder.bind(OntologyMappingProvider.class).toInstance(getOntologyMappingProvider(conf));
        binder.bind(OntologyTransformerProvider.class).toInstance(getTransformerProvider(conf));
    }

    //region Private Methods
    private AppUrlSupplier getAppUrlSupplier(Config conf) throws UnknownHostException {
        int applicationPort = conf.getInt("application.port");
        String baseUrl = String.format("http://%s:%d/opengraph", InetAddress.getLocalHost().getHostAddress(), applicationPort);
        if (conf.hasPath("appUrlSupplier.public.baseUri")) {
            baseUrl = conf.getString("appUrlSupplier.public.baseUri");
        }

        return new DefaultAppUrlSupplier(baseUrl);
    }

    /**
     * get ontology provider
     *
     * @param conf
     * @return
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    private OntologyProvider getOntologyProvider(Config conf) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        try {
            return new DirectoryOntologyProvider(conf.getString("opengraph.ontology_provider_dir"));
        } catch (ConfigException e1) {
            try {
                return (OntologyProvider) Class.forName(conf.getString("opengraph.ontology_provider")).getConstructor().newInstance();
            } catch (ConfigException e2) {
                throw new GraphError.GraphErrorException(new GraphError("No appropriate config value for { ontology_provider_dir | ontology_provider } found ",
                        "No appropriate config value for { ontology_provider_dir | ontology_mapping_provider } found "));
            }
        }
        //no ontology provider was found
    }

    /**
     * get ontology mapping provider
     *
     * @param conf
     * @return
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    private OntologyMappingProvider getOntologyMappingProvider(Config conf) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        try {
            String ontology_provider_dir = conf.getString("opengraph.ontology_provider_dir");
            return new DirectoryOntologyMappingProvider(ontology_provider_dir);
        } catch (ConfigException e1) {
            try {
                String ontology_mapping_provider = conf.getString("opengraph.ontology_mapping_provider");
                return (OntologyMappingProvider) Class.forName(ontology_mapping_provider).getConstructor().newInstance();
            } catch (ConfigException e2) {
                return new OntologyMappingProvider.VoidOntologyMappingProvider();
/*
                throw new FuseError.FuseErrorException(new FuseError("No appropriate config value for { ontology_provider_dir | ontology_mapping_provider } found ",
                        "No appropriate config value for { ontology_provider_dir | ontology_mapping_provider } found "));
*/
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        //no ontology mapping provider was found
    }

    private OntologyTransformerProvider getTransformerProvider(Config conf) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        try {
            return new DirectoryOntologyTransformerProvider(conf.getString("opengraph.ontology_provider_dir"));
        } catch (ConfigException e) {
            try {
                return (OntologyTransformerProvider) Class.forName(conf.getString("opengraph.ontology_transformation_provider")).getConstructor().newInstance();
            } catch (ConfigException.Missing missing) {
                return new VoidOntologyTransformerProvider();
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        //no ontology provider was found
    }
    //endregion
}
