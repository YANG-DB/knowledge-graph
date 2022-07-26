package org.unipop.process.strategyregistrar;








import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.util.DefaultTraversalStrategies;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.unipop.process.coalesce.UniGraphCoalesceStepStrategy;
import org.unipop.process.edge.EdgeStepsStrategy;
import org.unipop.process.optional.UniGraphOptionalStepStrategy;
import org.unipop.process.order.UniGraphOrderStrategy;
import org.unipop.process.properties.UniGraphPropertiesStrategy;
import org.unipop.process.repeat.UniGraphRepeatStepStrategy;
import org.unipop.process.start.UniGraphStartCountStepStrategy;
import org.unipop.process.start.UniGraphStartEdgeCountStepStrategy;
import org.unipop.process.start.UniGraphStartStepStrategy;
import org.unipop.process.strategy.CompositeStrategy;
import org.unipop.process.union.UniGraphUnionStepStrategy;
import org.unipop.process.vertex.UniGraphVertexStepStrategy;
import org.unipop.process.where.UniGraphWhereStepStrategy;

public class StandardStrategyProvider implements StrategyProvider {
    @Override
    public TraversalStrategies get() {
        DefaultTraversalStrategies traversalStrategies = new DefaultTraversalStrategies();
        traversalStrategies.addStrategies(
                new CompositeStrategy(Stream.of(
                new UniGraphStartStepStrategy(),
                new UniGraphStartCountStepStrategy(),
                new UniGraphVertexStepStrategy(),
                new UniGraphStartEdgeCountStepStrategy(),
                new EdgeStepsStrategy(),
                new UniGraphPropertiesStrategy(),
                new UniGraphCoalesceStepStrategy(),
                new UniGraphWhereStepStrategy(),
                new UniGraphUnionStepStrategy(),
                new UniGraphRepeatStepStrategy(),
                new UniGraphOrderStrategy(),
                new UniGraphOptionalStepStrategy()).toJavaList()
                ));
        TraversalStrategies.GlobalCache.getStrategies(Graph.class).toList().forEach(traversalStrategies::addStrategies);
        return traversalStrategies;
    }


}

