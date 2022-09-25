package org.opensearch.graph.epb.plan.statistics;

import com.google.inject.Provider;
import org.opensearch.graph.dispatcher.gta.PlanTraversalTranslator;
import org.opensearch.graph.epb.plan.statistics.configuration.EngineCountStatisticsConfig;
import org.opensearch.graph.executor.ontology.UniGraphProvider;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.entity.EConcrete;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.unipop.structure.UniGraph;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class EngineCountStatisticsProviderTest {

    @Test
    public void testNodeStatistics() throws Exception {
        PlanTraversalTranslator planTraversalTranslator = Mockito.mock(PlanTraversalTranslator.class);
        Ontology ontology = Mockito.mock(Ontology.class);
        Provider<UniGraphProvider> uniGraphProvider = Mockito.mock(Provider.class);
        UniGraphProvider uniGraphProvider1 = Mockito.mock(UniGraphProvider.class);
        UniGraph uniGraph = Mockito.mock(UniGraph.class);
        GraphTraversalSource source = Mockito.mock(GraphTraversalSource.class);
        when(uniGraphProvider.get()).thenReturn(uniGraphProvider1);
        when(uniGraphProvider1.getGraph(any())).thenReturn(uniGraph);
        when(uniGraph.traversal()).thenReturn(source);
        GraphTraversal traversal = Mockito.mock(GraphTraversal.class);
        when(planTraversalTranslator.translate(any(), any())).thenReturn(traversal);
        when(traversal.count()).thenReturn(traversal);
        when(traversal.next()).thenReturn(1L);
        EngineCountStatisticsConfig config = Mockito.mock(EngineCountStatisticsConfig.class);

        EngineCountStatisticsProvider provider = new EngineCountStatisticsProvider(planTraversalTranslator,
                ontology,
                uniGraphProvider,
                config
                );


        Statistics.SummaryStatistics nodeStatistics = provider.getNodeStatistics(new EConcrete());

        Assert.assertEquals(1, nodeStatistics.getCardinality(), 0.0001);
    }

}
