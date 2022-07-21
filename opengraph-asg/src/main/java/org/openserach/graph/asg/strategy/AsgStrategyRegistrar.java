package org.openserach.graph.asg.strategy;




/**
 * Created by lior.perry on 27/02/2017.
 */
public interface AsgStrategyRegistrar {
    Iterable<AsgStrategy> register();
}
