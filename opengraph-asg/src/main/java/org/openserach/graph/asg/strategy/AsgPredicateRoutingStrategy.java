package org.openserach.graph.asg.strategy;





import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.query.EBase;
import javaslang.collection.Stream;

import java.util.function.Function;
import java.util.function.Predicate;

public class AsgPredicateRoutingStrategy<T extends EBase> implements AsgStrategy {
    //region Constructors
    public AsgPredicateRoutingStrategy(Iterable<Routing<T>> routings, Function<AsgQuery, Iterable<AsgEBase<T>>> elementSelector) {
        this.routings = routings;
        this.elementSelector = elementSelector;
    }
    //endregion

    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Stream.ofAll(this.elementSelector.apply(query))
                .forEach(selectedElement -> {
                    Stream.ofAll(routings)
                            .filter(routing -> routing.predicate.test(selectedElement))
                            .toJavaOptional()
                            .ifPresent(routing -> routing.getStrategy().apply(query, selectedElement, context));

                });
    }
    //endregion

    //region Fields
    private Iterable<Routing<T>> routings;
    private Function<AsgQuery, Iterable<AsgEBase<T>>> elementSelector;
    //endregion

    public static class Routing<T extends EBase> {
        //region Constructors
        public Routing(Predicate<AsgEBase<T>> predicate, AsgElementStrategy<T> strategy) {
            this.predicate = predicate;
            this.strategy = strategy;
        }
        //endregion

        //region Properties
        public Predicate<AsgEBase<T>> getPredicate() {
            return predicate;
        }

        public AsgElementStrategy<T> getStrategy() {
            return strategy;
        }
        //endregion

        //region Fields
        private Predicate<AsgEBase<T>> predicate;
        private AsgElementStrategy<T> strategy;
        //endregion
    }
}
