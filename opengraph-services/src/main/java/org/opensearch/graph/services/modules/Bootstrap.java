package org.opensearch.graph.services.modules;


import com.codahale.metrics.MetricRegistry;
import com.google.inject.Binder;
import com.google.inject.matcher.Matchers;
import org.opensearch.graph.dispatcher.utils.*;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by lior.perry on 22/02/2017.
 * <p>
 * This module is called by the fuse-service scanner class loader
 */
public class Bootstrap implements Jooby.Module {
    @Override
    public void configure(Env env, Config conf, Binder binder) throws Throwable {

        binder.bindInterceptor(Matchers.any(), Matchers.annotatedWith(TimerAnnotation.class),
                new PerformanceStatistics(binder.getProvider(MetricRegistry.class)));

        //load modules according getTo configuration
        loadModules(env, conf, binder);
    }

    private void loadModules(Env env, Config conf, Binder binder) {
        String profile = conf.getString("application.profile");
        System.out.println("Active Profile " + profile);
        System.out.println("Loading modules: " + "modules." + profile);
        List<String> modules = conf.getStringList("modules." + profile);
        modules.forEach(value -> {
            try {
                Method method = Jooby.Module.class.getMethod("configure", Env.class, Config.class, Binder.class);
                method.invoke(Class.forName(value).newInstance(), env, conf, binder);
                System.out.println(" -- Module "+value +" loaded");
            } catch (Exception e) {
                //todo something useful here - should the app break ???
                System.out.println("ERROR - Bootstrap loading failed "+e.getMessage());
                e.printStackTrace();
            }
        });
        System.out.println("All modules loaded[" + modules.size()+"]");
    }
}
