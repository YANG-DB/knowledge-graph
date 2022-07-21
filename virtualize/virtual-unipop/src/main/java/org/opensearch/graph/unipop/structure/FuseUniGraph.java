package org.opensearch.graph.unipop.structure;



import org.opensearch.graph.unipop.process.traversal.dsl.graph.FuseGraphTraversalSource;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.unipop.process.strategyregistrar.StrategyProvider;
import org.unipop.query.controller.ControllerManagerFactory;
import org.unipop.structure.UniGraph;

public class FuseUniGraph extends UniGraph {
    //region Constructors
    public FuseUniGraph(Configuration configuration, ControllerManagerFactory controllerManagerFactory, StrategyProvider strategyProvider) throws Exception {
        super(configuration, controllerManagerFactory, strategyProvider);
    }
    //endregion

    //region Override Methods
    @Override
    public GraphTraversalSource traversal() {
        return new FuseGraphTraversalSource(this, this.strategies);
        //return new GraphTraversalSource(this, strategies);
    }
    //endregion
}
