package org.openserach.graph.asg;







import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.typesafe.config.Config;
import org.openserach.graph.asg.validation.AsgQueryValidator;
import org.openserach.graph.asg.validation.AsgValidatorStrategyRegistrar;
import org.openserach.graph.asg.validation.AsgValidatorStrategyRegistrarImpl;
import org.opensearch.graph.dispatcher.modules.ModuleBase;
import org.opensearch.graph.dispatcher.validation.QueryValidator;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.jooby.Env;

public class AsgValidationModule extends ModuleBase {
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        Class<? extends AsgValidatorStrategyRegistrar> asgValidatorStrategyRegistrar = AsgValidatorStrategyRegistrarImpl.class;
        try {
            asgValidatorStrategyRegistrar = getAsgValidatorStrategyRegistrar(conf);
        } catch (Throwable e) {
            //failed binding to external class - use default
        }

        binder.bind(AsgValidatorStrategyRegistrar.class)
                .to(asgValidatorStrategyRegistrar)
                .asEagerSingleton();

        binder.bind(new TypeLiteral<QueryValidator<AsgQuery>>() {})
                .to(AsgQueryValidator.class)
                .asEagerSingleton();
    }


    protected Class<? extends AsgValidatorStrategyRegistrar> getAsgValidatorStrategyRegistrar(Config conf) throws ClassNotFoundException {
        return (Class<? extends AsgValidatorStrategyRegistrar>) Class.forName(conf.getString(conf.getString("assembly") + ".asg_validator_strategy_registrar"));
    }
}
