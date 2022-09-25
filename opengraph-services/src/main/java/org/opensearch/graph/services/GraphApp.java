package org.opensearch.graph.services;

/*-
 * #%L
 * opengraph-services
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




/*-
 *
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import org.opensearch.graph.dispatcher.urlSupplier.AppUrlSupplier;
import org.opensearch.graph.epb.plan.statistics.Statistics;
import org.opensearch.graph.logging.StatusReportedJob;
import org.opensearch.graph.services.appRegistrars.*;
import javaslang.Tuple2;
import org.jooby.Jooby;
import org.jooby.RequestLogger;
import org.jooby.Results;
import org.jooby.caffeine.CaffeineCache;
import org.jooby.handlers.AssetHandler;
import org.jooby.handlers.CorsHandler;
import org.jooby.metrics.Metrics;
import org.jooby.quartz.Quartz;
import org.jooby.scanner.Scanner;
import org.reflections.Reflections;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;


@SuppressWarnings({"unchecked", "rawtypes"})
public class GraphApp extends Jooby {
    //region Consructors
    public GraphApp(AppUrlSupplier localUrlSupplier) {
        use(new Scanner());

        use("*", new RequestLogger().extended());
        //metrics statistics
        MetricRegistry metricRegistry = new MetricRegistry();
        bind(metricRegistry);

        // Timer example:
        use(new Metrics(metricRegistry)
                .request()
                .threadDump()
                .ping()
                .metric("memory", new MemoryUsageGaugeSet())
                .metric("threads", new ThreadStatesGaugeSet())
                .metric("gc", new GarbageCollectorMetricSet()));

        use(use(new CaffeineCache<Tuple2<String, List<String>>, List<Statistics.BucketInfo>>() {}));
        // swagger support
        get("swagger/swagger.json", () -> Results.redirect("/public/assets/swagger/swagger.json"));
        get("redocly/redocly", () -> Results.redirect("/public/assets/redocly/redocly.html"));
        // opensearch bigDesk support (
        get("bigdesk", () -> Results.redirect("/public/assets/bigdesk/index.html"));
        get("queryBuilder/sparql", () -> Results.redirect("/public/assets/query/sparql/index.html"));
        get("queryBuilder/graphql", () -> Results.redirect("/public/assets/query/graphql/index.html"));
        get("queryBuilder/cypher", () -> Results.redirect("/public/assets/query/cypher/index.html"));

        //internal quarts reporting job scheduler
        use(new Quartz().with(StatusReportedJob.class));

        //'Access-Control-Allow-Origin' header
        use("*", new CorsHandler());
        //expose html assets
        assets("public/**",new AssetHandler(Paths.get("public")));
        assets("/assets/**");
        assets("public/assets/**");
        assets("public/assets/samples/**");
        assets("public/assets/lib/**");

        new LoggingJacksonRendererRegistrar(metricRegistry).register(this, localUrlSupplier);
        new BeforeAfterAppRegistrar().register(this, localUrlSupplier);
        new CorsAppRegistrar().register(this, localUrlSupplier);
        new HomeAppRegistrar().register(this, localUrlSupplier);
        new HealthAppRegistrar().register(this, localUrlSupplier);

        //dynamically load AppControllerRegistrar that comply with org.opensearch.graph.services package and derive from AppControllerRegistrarBase
        additionalRegistrars(this, localUrlSupplier);

        //callbacks
        onStop(() -> System.out.println("Stopping Opengraph app..."));
        onStart(() -> System.out.println("Starting Opengraph app..."));
        onStarted(() -> System.out.println("Opengraph Started on http://localhost:8888/"));
    }

    /**
     * dynamically load AppControllerRegistrar that comply with org.opensearch.graph.services package and derive from AppControllerRegistrarBase
     *
     * @param graphApp
     * @param localUrlSupplier
     */
    private void additionalRegistrars(GraphApp graphApp, AppUrlSupplier localUrlSupplier) {
        Reflections reflections = new Reflections(GraphApp.class.getPackage().getName());
        Set<Class<? extends AppControllerRegistrarBase>> allClasses = reflections.getSubTypesOf(AppControllerRegistrarBase.class);
        allClasses.forEach(clazz -> {
            try {
                clazz.getConstructor().newInstance().register(graphApp, localUrlSupplier);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }
    //endregion


    //region Public Methods
    public GraphApp conf(File file, String activeProfile, Tuple2<String, ConfigValue>... values) {
        Config config = GraphUtils.loadConfig(file, activeProfile, values);
        super.use(config);
        return this;
    }

    public GraphApp conf(Config config) {
        super.use(config);
        return this;
    }

    //endregion
}
