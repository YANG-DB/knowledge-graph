package org.openserach.graph.asg.validation;




public interface AsgValidatorStrategyRegistrar {
    Iterable<AsgValidatorStrategy> register();
}
