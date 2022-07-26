package org.opensearch.graph.asg;





import com.google.inject.Binder;
import org.opensearch.graph.dispatcher.query.JsonQueryTransformerFactory;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.openserach.graph.asg.AsgModule;
import org.openserach.graph.asg.strategy.AsgStrategyRegistrar;
import org.openserach.graph.asg.translator.BasicJsonQueryTransformerFactory;

public class M2AsgModule extends AsgModule {
    //region ModuleBase Implementation
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        super.configureInner(env,conf,binder);
        //configure the different query languages transformer factory
        binder.bind(JsonQueryTransformerFactory.class).to(BasicJsonQueryTransformerFactory.class);
    }
    //endregion


    protected Class<? extends AsgStrategyRegistrar> getAsgStrategyRegistrar(Config conf) throws ClassNotFoundException {
        return (Class<? extends  AsgStrategyRegistrar>)Class.forName(conf.getString(conf.getString("assembly")+".asg_strategy_registrar"));
    }
}
