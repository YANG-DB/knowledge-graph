package org.openserach.graph.asg.validation;




/**
 * Created by lior.perry on 27/02/2017.
 */
public interface AsgValidatorStrategyRegistrar {
    Iterable<AsgValidatorStrategy> register();
}
