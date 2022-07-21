package org.openserach.graph.asg.strategy;




import java.util.Arrays;

/**
 * Created by lior.perry on 05/03/2017.
 */
public class DefaultAsgStrategyRegistrar implements AsgStrategyRegistrar {
    //region AsgStrategyRegistrar Implementation
    @Override
    public Iterable<AsgStrategy> register() {
        return Arrays.asList(
                new DummyAsgStrategy()
        );
    }
    //endregion
}
