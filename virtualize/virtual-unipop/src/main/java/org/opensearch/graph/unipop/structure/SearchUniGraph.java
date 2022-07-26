package org.opensearch.graph.unipop.structure;





import org.opensearch.graph.unipop.process.traversal.dsl.graph.SearchGraphTraversalSource;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.unipop.process.strategyregistrar.StrategyProvider;
import org.unipop.query.controller.ControllerManagerFactory;
import org.unipop.structure.UniGraph;

public class SearchUniGraph extends UniGraph {
    //region Constructors
    public SearchUniGraph(Configuration configuration, ControllerManagerFactory controllerManagerFactory, StrategyProvider strategyProvider) throws Exception {
        super(configuration, controllerManagerFactory, strategyProvider);
    }
    //endregion

    //region Override Methods
    @Override
    public GraphTraversalSource traversal() {
        return new SearchGraphTraversalSource(this, this.strategies);
        //return new GraphTraversalSource(this, strategies);
    }
    //endregion
}
