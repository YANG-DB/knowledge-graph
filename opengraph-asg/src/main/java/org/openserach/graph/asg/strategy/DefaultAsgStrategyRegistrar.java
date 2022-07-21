package org.openserach.graph.asg.strategy;





import java.util.Arrays;

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
